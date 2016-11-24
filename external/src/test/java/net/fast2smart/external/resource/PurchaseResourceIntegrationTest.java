package net.fast2smart.external.resource;

import com.jayway.jsonassert.JsonAssert;
import net.fast2smart.external.AbstractKafkaIntegrationTest;
import net.fast2smart.external.model.ExternalMember;
import net.fast2smart.external.model.PurchaseEvent;
import net.fast2smart.legacy.model.Partner;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PurchaseResourceIntegrationTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private TestRestTemplate client;

    @Test
    public void testPurchase() throws InterruptedException {
        LocalDateTime purchaseDate = LocalDateTime.now();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        ResponseEntity<ExternalMember> member = client.postForEntity("/members", new HttpEntity<>(String.format("{\"lastname\":\"Helbig\",\"firstname\":\"Markus\",\"card\":{\"number\":3201670657774,\"partner\":\"HOLIDAY\"},\"enrolmentDate\":\"%s\"}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)), headers), ExternalMember.class);
        assertThat(member.getStatusCode(), equalTo(HttpStatus.OK));

        assertThat(records.poll(10, TimeUnit.SECONDS), notNullValue()); //clear enrolments from test records

        ResponseEntity<PurchaseEvent> response = client.postForEntity("/purchases", new HttpEntity<>(String.format("{\"cardnumber\":3201670657774,\"partner\":\"HOLIDAY\",\"amount\":4.99,\"currency\":\"EUR\",\"basePoints\":5,\"statusPoints\":45,\"date\":\"%s\"}", purchaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)), headers), PurchaseEvent.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        PurchaseEvent resultPurchase = response.getBody();
        assertThat(resultPurchase.getPartner(), equalTo(Partner.HOLIDAY));
        assertThat(resultPurchase.getAmount(), equalTo(BigDecimal.valueOf(4.99)));
        assertThat(resultPurchase.getCurrency(), equalTo(Currency.getInstance("EUR")));
        assertThat(resultPurchase.getBasePoints(), equalTo(5L));
        assertThat(resultPurchase.getstatusPoints(), equalTo(45L));
        assertThat(resultPurchase.getCardnumber(), equalTo(3201670657774L));
        assertThat(resultPurchase.getDate(), equalTo(purchaseDate));
        assertThat(resultPurchase.getMemberId(), equalTo(member.getBody().getMemberId()));

        ConsumerRecord<Long, String> result = records.poll(10, TimeUnit.SECONDS);
        assertThat(result, notNullValue());
        JsonAssert.with(result.value())
                .assertNotNull("id").and()
                .assertNotNull("member.id").and()
                .assertEquals("member.cardnumber", 3201670657774L).and()
                .assertEquals("partner", "HOLIDAY").and()
                .assertEquals("amount", 4.99).and()
                .assertEquals("currency", "EUR").and()
                .assertEquals("basePoints", 5).and()
                .assertEquals("statusPoints", 45).and()
                .assertEquals("date", purchaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        ResponseEntity<List<PurchaseEvent>> responseList = client.exchange("/members/3201670657774/purchases", HttpMethod.GET, null, new ParameterizedTypeReference<List<PurchaseEvent>>() {
        });
        assertThat(responseList.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(responseList.getBody(), hasSize(1));
        assertThat(responseList.getBody().get(0), equalTo(resultPurchase));
    }
}

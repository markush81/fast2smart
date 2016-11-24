package net.fast2smart.external.resource;

import com.jayway.jsonassert.JsonAssert;
import net.fast2smart.external.AbstractKafkaIntegrationTest;
import net.fast2smart.external.model.ExternalMember;
import net.fast2smart.legacy.model.Partner;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hamcrest.CustomMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MemberResourceIntegrationTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private TestRestTemplate client;

    @Test
    public void testEnrolMember() throws InterruptedException {
        LocalDateTime enrolmentDate = LocalDateTime.now();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        ResponseEntity<ExternalMember> response = client.postForEntity("/members", new HttpEntity<>(String.format("{\"lastname\":\"Helbig\",\"firstname\":\"Markus\",\"card\":{\"number\":3207595640976,\"partner\":\"HOLIDAY\"},\"enrolmentDate\":\"%s\"}", enrolmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)), headers), ExternalMember.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), new CustomMatcher<ExternalMember>("") {
            @Override
            public boolean matches(Object item) {
                ExternalMember externalMember = (ExternalMember) item;
                return externalMember.getLastname().equals("Helbig") &&
                        externalMember.getFirstname().equals("Markus") &&
                        externalMember.getCard().getPartner().equals(Partner.HOLIDAY) &&
                        externalMember.getCard().getNumber().equals(3207595640976L) &&
                        externalMember.getEnrolmentDate().equals(enrolmentDate);
            }
        });

        ConsumerRecord<Long, String> result = records.poll(10, TimeUnit.SECONDS);
        assertThat(result, notNullValue());
        JsonAssert.with(result.value())
                .assertNotNull("id").and()
                .assertEquals("lastname", "Helbig").and()
                .assertEquals("firstname", "Markus").and()
                .assertEquals("cardnumber", 3207595640976L).and()
                .assertEquals("partner", "HOLIDAY").and()
                .assertEquals("enrolmentDate", enrolmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}

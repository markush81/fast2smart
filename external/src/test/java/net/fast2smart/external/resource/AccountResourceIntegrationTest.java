package net.fast2smart.external.resource;

import net.fast2smart.external.AbstractKafkaIntegrationTest;
import net.fast2smart.external.model.AccountStatement;
import net.fast2smart.external.model.ExternalMember;
import net.fast2smart.external.model.PurchaseEvent;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountResourceIntegrationTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private TestRestTemplate client;

    @Test
    public void testPurchase() throws InterruptedException {
        LocalDateTime enrolmentDate = LocalDateTime.now();
        LocalDateTime purchaseDate = LocalDateTime.now();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        ResponseEntity<ExternalMember> member = client.postForEntity("/members", new HttpEntity<>(String.format("{\"lastname\":\"Helbig\",\"firstname\":\"Markus\",\"card\":{\"number\":3201670657774,\"partner\":\"BOOKS\"},\"enrolmentDate\":\"%s\"}", enrolmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)), headers), ExternalMember.class);
        assertThat(member.getStatusCode(), equalTo(HttpStatus.OK));


        ResponseEntity<AccountStatement> account = client.getForEntity("/members/3201670657774/account", AccountStatement.class);
        assertThat(account.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(account.getBody().getCardnumber(), equalTo(3201670657774L));
        assertThat(account.getBody().getBasePoints(), equalTo(0L));
        assertThat(account.getBody().getstatusPoints(), equalTo(0L));
        assertThat(account.getBody().getLastUpdate(), equalTo(enrolmentDate));

        ResponseEntity<PurchaseEvent> response = client.postForEntity("/purchases", new HttpEntity<>(String.format("{\"cardnumber\":3201670657774,\"partner\":\"BOOKS\",\"amount\":4.99,\"currency\":\"EUR\",\"basePoints\":5,\"statusPoints\":45,\"date\":\"%s\"}", purchaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)), headers), PurchaseEvent.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        account = client.getForEntity("/members/3201670657774/account", AccountStatement.class);
        assertThat(account.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(account.getBody().getCardnumber(), equalTo(3201670657774L));
        assertThat(account.getBody().getBasePoints(), equalTo(5L));
        assertThat(account.getBody().getstatusPoints(), equalTo(45L));
        assertThat(account.getBody().getLastUpdate(), equalTo(purchaseDate));
    }
}

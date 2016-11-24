package net.fast2smart.external.resource;

import net.fast2smart.external.AbstractKafkaIntegrationTest;
import net.fast2smart.external.model.ExternalMember;
import net.fast2smart.external.model.ExternalTreatment;
import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.model.Partner;
import net.fast2smart.legacy.repository.MemberRepository;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TreatmentResourceIntegrationTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private TestRestTemplate client;
    @Autowired
    private MemberRepository memberRepository;
    private Producer<Long, String> producer;

    @Before
    public void setUp() {
        Map<String, Object> configs = KafkaTestUtils.producerProps(embeddedKafka);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producer = new KafkaProducer<>(configs);
    }

    @After
    public void tearDown() {
        producer.close();
    }

    @Test
    public void testTreatments() throws InterruptedException {
        LocalDateTime enrolmentDate = LocalDateTime.now();
        LocalDateTime beforeAssignment = LocalDateTime.now();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        ResponseEntity<ExternalMember> member = client.postForEntity("/members", new HttpEntity<>(String.format("{\"lastname\":\"Helbig\",\"firstname\":\"Markus\",\"card\":{\"number\":3201670657774,\"partner\":\"HOLIDAY\"},\"enrolmentDate\":\"%s\"}", enrolmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)), headers), ExternalMember.class);
        assertThat(member.getStatusCode(), equalTo(HttpStatus.OK));

        //get it from database to have id for sending proper record to treatments topic as streaming would do as well
        Member persistedMember = memberRepository.findByCardnumber(3201670657774L);
        producer.send(new ProducerRecord<>("treatments", persistedMember.getId(), String.format("{\"member\": %s, \"partner\": \"SUPERMARKET\", \"headline\":\"5x Points Booster\"}", persistedMember.getId())));
        //also two assignments are sent, only one treatment should be assigned
        producer.send(new ProducerRecord<>("treatments", persistedMember.getId(), String.format("{\"member\": %s, \"partner\": \"SUPERMARKET\", \"headline\":\"5x Points Booster\"}", persistedMember.getId())));

        //await until treatment is saved, it can take some time due to asynchronous dispatch via kafka
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
            ResponseEntity<List<ExternalTreatment>> response = client.exchange("/members/3201670657774/treatments", HttpMethod.GET, null, new ParameterizedTypeReference<List<ExternalTreatment>>() {
            });
            return response.getBody().size() == 1;
        });

        //and now let's check if contet is correct
        ResponseEntity<List<ExternalTreatment>> response = client.exchange("/members/3201670657774/treatments", HttpMethod.GET, null, new ParameterizedTypeReference<List<ExternalTreatment>>() {
        });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        List<ExternalTreatment> resultList = response.getBody();
        assertThat(resultList, hasSize(1));
        assertThat(resultList.get(0).getCardnumber(), equalTo(3201670657774L));
        assertThat(resultList.get(0).getPartner(), equalTo(Partner.SUPERMARKET));
        assertThat(resultList.get(0).getHeadline(), equalTo("5x Points Booster"));
        assertThat(resultList.get(0).getAssigned(), LocalDateTimeMatchers.after(beforeAssignment));
        assertThat(resultList.get(0).getAssigned(), LocalDateTimeMatchers.before(LocalDateTime.now()));
    }
}

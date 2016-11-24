package net.fast2smart.external;

import net.fast2smart.legacy.service.AccountService;
import net.fast2smart.legacy.service.MemberService;
import net.fast2smart.legacy.service.TreatmentService;
import org.apache.kafka.clients.producer.Producer;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.reset;

/**
 * Created by markus on 03/11/2016.
 */
public class AbstractMvcTest {

    @Autowired
    protected MockMvc mvc;
    @MockBean
    protected AccountService accountService;
    @MockBean
    protected MemberService memberService;
    @MockBean
    protected TreatmentService treatmentService;
    @MockBean
    protected KafkaTemplate<Long, String> kafkaTemplate;
    @MockBean
    protected Producer<Long, String> kafkaProducer;

    @Before
    public void setUp() throws Exception {
        reset(kafkaTemplate, kafkaProducer, memberService, accountService, treatmentService);
    }

    @After
    public void tearDown() throws Exception {

    }
}

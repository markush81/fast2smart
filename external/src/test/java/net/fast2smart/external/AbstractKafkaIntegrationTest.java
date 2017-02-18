package net.fast2smart.external;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by markus on 03/11/2016.
 */
@DirtiesContext
@ContextConfiguration
@SuppressWarnings({"squid:S2187"})
public class AbstractKafkaIntegrationTest {

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(2, true, 4, "enrolments", "purchases", "treatments");

    protected BlockingQueue<ConsumerRecord<Long, String>> records;
    private KafkaMessageListenerContainer<Long, String> container;

    @Before
    public void setUp() throws Exception {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        DefaultKafkaConsumerFactory<Long, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        container = new KafkaMessageListenerContainer<>(cf, new ContainerProperties("enrolments", "purchases", "treatments"));
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<Long, String>) record -> records.add(record));
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic() * 3); // we create three topics
    }

    @After
    public void tearDown() {
        container.stop();
    }

    @TestConfiguration
    public static class TestApplicationConfiguration {

        @Autowired
        private KafkaProperties kafkaProperties;

        @Bean
        public Map<String, Object> producerConfigs() {
            Map<String, Object> producerConfigs = kafkaProperties.buildProducerProperties();
            producerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaTestUtils.producerProps(embeddedKafka).get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            return producerConfigs;
        }

        @Bean
        public Map<String, Object> consumerConfigs() {
            Map<String, Object> consumerConfigs = kafkaProperties.buildConsumerProperties();
            consumerConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaTestUtils.producerProps(embeddedKafka).get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
            consumerConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name().toLowerCase()); //this is specifically for test, because test might have produced record before consumer is ready
            return consumerConfigs;
        }
    }
}

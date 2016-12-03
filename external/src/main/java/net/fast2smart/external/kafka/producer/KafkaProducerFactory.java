package net.fast2smart.external.kafka.producer;

import net.fast2smart.external.config.ApplicationConfiguration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaProducerFactory<K, V> {

    @Autowired
    private ApplicationConfiguration configuration;

    @Bean
    @SuppressWarnings("WeakerAccess")
    protected ProducerFactory<K, V> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    @SuppressWarnings("WeakerAccess")
    protected Map<String, Object> producerConfigs() {
        return configuration.getProducerProperties();
    }

    /**
     * Using spring-kafka
     *
     * @return KafkaTemplate
     */
    @Bean
    public KafkaTemplate<K, V> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Standardway, just create a new instance
     *
     * @return KafkaProducer
     */
    @Bean
    public Producer<K, V> kafkaProducer() {
        return new KafkaProducer<>(producerConfigs());
    }

}

package net.fast2smart.external.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaProducerFactory<K, V> {

    private final KafkaProperties kafkaProperties;

    public KafkaProducerFactory(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    @SuppressWarnings("WeakerAccess")
    protected Map<String, Object> producerConfigs() {
        return kafkaProperties.buildProducerProperties();
    }

    @Bean
    @SuppressWarnings("WeakerAccess")
    protected ProducerFactory<K, V> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<K, V> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public Producer<K, V> kafkaProducer() {
        return new KafkaProducer<>(producerConfigs());
    }

}

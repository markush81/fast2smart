package net.fast2smart.external.kafka.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

@Configuration
public class KafkaConsumerFactory<K, V> {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerFactory(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    @SuppressWarnings("WeakerAccess")
    protected Map<String, Object> consumerConfigs() {
        return kafkaProperties.buildConsumerProperties();
    }

    @Bean
    @SuppressWarnings("WeakerAccess")
    protected ConsumerFactory<K, V> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * KafkaConsumers are not thread-safe so either you have to create on per thread or just create one and synchronize usage
     *
     * @return KafkaConsumer
     */
    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Consumer<K, V> kafkaConsumer() {
        return consumerFactory().createConsumer();
    }

}

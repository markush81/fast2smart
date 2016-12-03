package net.fast2smart.external.kafka.consumer;

import net.fast2smart.external.config.ApplicationConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Map;

@Configuration
public class KafkaConsumerFactory<K, V> {

    @Autowired
    private ApplicationConfiguration configuration;

    @Bean
    @SuppressWarnings("WeakerAccess")
    protected Map<String, Object> consumerConfigs() {
        return configuration.getConsumerProperties();
    }

    /**
     * KafkaConsumers are not thread-safe so either you have to create on per thread or just create one and synchronize usage
     *
     * @return KafkaConsumer
     */
    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Consumer<K, V> kafkaConsumer() {
        return new KafkaConsumer<>(consumerConfigs());
    }

}

package net.fast2smart.external.config;

import com.google.common.collect.Maps;
import net.fast2smart.external.Application;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties
@EnableConfigurationProperties
@EnableScheduling
@EnableJpaRepositories(basePackages = {"net.fast2smart.legacy.repository"})
@EntityScan(basePackageClasses = {Application.class, Jsr310JpaConverters.class}, basePackages = {"net.fast2smart.legacy.model"})
public class ApplicationConfiguration {

    private List<String> brokers;
    private Map<String, Object> producer;
    private Map<String, Object> consumer;

    public Map<String, Object> getProducer() {
        return producer;
    }

    public void setProducer(Map<String, Object> producer) {
        this.producer = producer;
    }

    public Map<String, Object> getConsumer() {
        return consumer;
    }

    public void setConsumer(Map<String, Object> consumer) {
        this.consumer = consumer;
    }

    public List<String> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<String> brokers) {
        this.brokers = brokers;
    }

    public Map<String, Object> getProducerProperties() {
        Map<String, Object> properties = flatProperties(producer, Maps.newHashMap(), null);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return properties;
    }

    public Map<String, Object> getConsumerProperties() {
        Map<String, Object> properties = flatProperties(consumer, Maps.newHashMap(), null);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return properties;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> flatProperties(Map<String, Object> input, Map<String, Object> result, String current) {
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            if (entry.getValue() instanceof Map) {
                if (current == null) {
                    flatProperties((Map<String, Object>) entry.getValue(), result, entry.getKey());
                } else {
                    flatProperties((Map<String, Object>) entry.getValue(), result, current + "." + entry.getKey());
                }
            } else if (current == null) {
                result.put(entry.getKey(), entry.getValue());
            } else {
                result.put(current + "." + entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}

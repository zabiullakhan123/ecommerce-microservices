package com.shop.common.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Shared Kafka setup imported by every event-driven service:
 * creates the saga topics, and retries failed messages 3 times
 * before parking them on "&lt;topic&gt;.DLT" (dead-letter topic).
 */
@Configuration
public class KafkaCommonConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaCommonConfig.class);

    @Bean
    public KafkaAdmin.NewTopics sagaTopics() {
        return new KafkaAdmin.NewTopics(Topics.ALL.stream()
                .map(name -> TopicBuilder.name(name).partitions(3).replicas(1).build())
                .toArray(NewTopic[]::new));
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        handler.setRetryListeners((record, ex, attempt) ->
                log.warn("Retry {} for {} key={}: {}", attempt, record.topic(), record.key(), ex.getMessage()));
        return handler;
    }
}

package com.kart.delivery.config;

import com.kart.delivery.kafka.event.OrderEvent;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> orderKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderEvent> consumerFactory,
            KafkaTemplate<Object, Object> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, this::getDeadLetterTopic);
        recoverer.setLogRecoveryRecord(true);
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2L);

        CommonErrorHandler errorHandler = new DefaultErrorHandler(recoverer, fixedBackOff);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    private TopicPartition getDeadLetterTopic(org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record, Exception exception) {
        return new TopicPartition(
                record.topic() + "-dlt",
                record.partition()
        );
    }
}

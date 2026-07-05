package com.springcloud.payment_service.config;

import com.springcloud.payment_service.dto.PaymentEvent;
import com.springcloud.payment_service.dto.PaymentResultEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.payment-result-topic.name}")
    private String paymentResultTopicName;

    @Value("${spring.kafka.payment-dead-letter-topic.name}")
    private String paymentDeadLetterTopicName;

    @Bean
    public NewTopic paymentResultTopic() {
        return TopicBuilder.name(paymentResultTopicName).build();
    }

    @Bean
    public NewTopic paymentDeadLetterTopic() {
        return TopicBuilder.name(paymentDeadLetterTopicName).build();
    }

    @Bean
    public ProducerFactory<String, PaymentResultEvent> paymentResultProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, PaymentResultEvent> paymentResultKafkaTemplate(
            ProducerFactory<String, PaymentResultEvent> paymentResultProducerFactory) {
        return new KafkaTemplate<>(paymentResultProducerFactory);
    }

    @Bean
    public ProducerFactory<String, PaymentEvent> paymentDeadLetterProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, PaymentEvent> paymentDeadLetterKafkaTemplate(
            ProducerFactory<String, PaymentEvent> paymentDeadLetterProducerFactory) {
        return new KafkaTemplate<>(paymentDeadLetterProducerFactory);
    }
}

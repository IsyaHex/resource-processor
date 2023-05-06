package uz.epam.msa.processor.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Producer {

    private final NewTopic topic;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public Producer(NewTopic topic, KafkaTemplate<String, String> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String resourceId) {
        log.info(String.format(Constants.RECEIVED_RESOURCE_ID, resourceId));
        Message<String> message = MessageBuilder
                .withPayload(resourceId)
                .setHeader(KafkaHeaders.TOPIC, topic.name())
                .build();
        kafkaTemplate.send(message);
    }
}
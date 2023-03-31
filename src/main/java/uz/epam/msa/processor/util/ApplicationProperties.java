package uz.epam.msa.processor.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@RefreshScope
public class ApplicationProperties {
    @Value("${app.kafka.group.id}")
    private String groupId;

    @Value("${app.kafka.topic.name}")
    private String topicName;

    @Value("${app.kafka.bootstrap.server}")
    private String bootstrapServer;
}

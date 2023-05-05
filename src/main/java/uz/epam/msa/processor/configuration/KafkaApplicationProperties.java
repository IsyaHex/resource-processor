package uz.epam.msa.processor.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@RefreshScope
@ConfigurationProperties
public class KafkaApplicationProperties {
    @Value("${app.kafka.group.id}")
    private String groupId;

    @Value("${app.kafka.bootstrap.server}")
    private String bootstrapServer;
}

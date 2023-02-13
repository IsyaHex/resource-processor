package uz.epam.msa.processor.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import uz.epam.msa.processor.controller.ProcessorController;

import java.util.Arrays;

@Component
@Slf4j
public class ResourceConsumer {

    private final ProcessorController controller;

    public ResourceConsumer(ProcessorController controller) {
        this.controller = controller;
    }

    // create 2 bindings, with 2 input and output channels
    // binding 1 -> resource processor sends resourceId to resource-service and gets metadata
    // binding 2 -> received metadata with resourceId getting sent to song service
    @KafkaListener(topics = "${app.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(Integer resourceId) {
        log.info(String.format("Resource id => %s", resourceId));
        byte[] songMetadata = controller.getResourceMetadata(resourceId);
        log.info(String.format("Result length from resource service => %s", songMetadata.length));
        byte[] result = controller.updateSongMetadata(resourceId, songMetadata);
        log.info(String.format("Result length from song service => %s", Arrays.toString(result)));
    }
}

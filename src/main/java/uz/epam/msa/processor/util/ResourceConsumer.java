package uz.epam.msa.processor.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uz.epam.msa.processor.dto.ResourceDTO;
import uz.epam.msa.processor.dto.SongDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class ResourceConsumer {

    @Value("${api.gateway.resources.endpoint.url}")
    private String API_GATEWAY_RESOURCES_SERVICE_URL;
    @Value("${api.gateway.songs.endpoint.url}")
    private String API_GATEWAY_SONG_SERVICE_URL;

    @Value("${app.resource.topic.name}")
    private String topicName;

    private final Producer producer;
    private final RestTemplate restTemplate;
    public ResourceConsumer(Producer producer, RestTemplate restTemplate) {
        this.producer = producer;
        this.restTemplate = restTemplate;
    }


    @KafkaListener(topics = "resources-id-topic")
    @Retryable(value = Exception.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000L))
    public void consume(String data) {
        log.info(String.format(Constants.RECEIVED_RESOURCE_DATA, data));
        Map<String, String> resourceData = parseResourceData(data);
        String token = resourceData.get(Constants.TOKEN);
        byte[] blob = getResourceObject(resourceData);
        SongDTO dto = createFile(blob, resourceData.get(Constants.RESOURCE_ID));
        log.info(String.format(Constants.SONG_DTO_FORMED_MSG, dto));
        producer.sendMessage(String.valueOf(postSongMetadata(dto, token)));
    }

    public byte[] getResourceObject(Map<String, String> resourceData) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, resourceData.get(Constants.TOKEN));
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(API_GATEWAY_RESOURCES_SERVICE_URL + resourceData.get(Constants.RESOURCE_ID), HttpMethod.GET, request, byte[].class);
        log.info(String.format("Response status -> %s", response.getStatusCodeValue()));
        return response.getBody();
    }

    public int postSongMetadata(SongDTO dto, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, token);
        HttpEntity<SongDTO> request = new HttpEntity<>(dto, headers);

        ResponseEntity<ResourceDTO> response = restTemplate.exchange(API_GATEWAY_SONG_SERVICE_URL, HttpMethod.POST, request, ResourceDTO.class);
        log.info(String.format("Response status -> %s", response.getStatusCodeValue()));
        return Objects.requireNonNull(response.getBody()).getId();
    }

    @Recover
    public void consumeRecover(Exception e, String resourceID) {
        log.error(e.getMessage(), e);
    }

    private SongDTO createFile(byte[] file, String resourceID) {
        SongDTO dto = new SongDTO();
        try (InputStream inputStream = new ByteArrayInputStream(file)) {
            ContentHandler handler = new DefaultHandler();
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();
            ParseContext parseCtx = new ParseContext();
            parser.parse(inputStream, handler, metadata, parseCtx);
            dto.setResourceId(resourceID);
            dto.setName(metadata.get(Constants.SONG_NAME));
            dto.setArtist(metadata.get(Constants.SONG_ARTIST));
            dto.setAlbum(metadata.get(Constants.SONG_ALBUM));
            dto.setLength(metadata.get(Constants.SONG_LENGTH));
            dto.setYear(getCurrentTime());
        }
        catch (TikaException | IOException | SAXException e) {
            log.error(e.getMessage(), e);
        }
        return dto;
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.GET_TIME_PATTERN);
        LocalDateTime dateTime = LocalDateTime.now();
        return formatter.format(dateTime);
    }

    private Map<String, String> parseResourceData(String s) {
        Map<String, String> data = new HashMap<>();
        String resourceId = s.substring(2, s.indexOf(','));
        String token = s.substring(s.indexOf(',') + 1, s.indexOf(']')).strip();

        data.put(Constants.RESOURCE_ID, resourceId);
        data.put(Constants.TOKEN, token);
        return data;
    }

}

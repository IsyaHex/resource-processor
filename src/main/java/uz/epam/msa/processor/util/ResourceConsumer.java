package uz.epam.msa.processor.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uz.epam.msa.processor.Constants;
import uz.epam.msa.processor.dto.ResourceDTO;
import uz.epam.msa.processor.dto.SongDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class ResourceConsumer {

    @Value("${api.gateway.resources.endpoint.url}")
    private String API_GATEWAY_RESOURCES_SERVICE_URL;
    @Value("${api.gateway.songs.endpoint.url}")
    private String API_GATEWAY_SONG_SERVICE_URL;


    @KafkaListener(topics = "resources-id-topic")
    @Retryable(value = Exception.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000L))
    public void consume(String resourceID) {
        log.info(String.format(Constants.RECEIVED_RESOURCE_ID, resourceID));
        byte[] data = getResourceObject(resourceID);
        SongDTO dto = createFile(data, resourceID);
        log.info(String.format(Constants.SONG_DTO_FORMED_MSG, dto));
        postSongMetadata(dto);
    }

    public byte[] getResourceObject(String resourceID) {
        ResponseEntity<byte[]> response = MicroserviceUtil.getInstanceRestTemplate().getForEntity(API_GATEWAY_RESOURCES_SERVICE_URL + resourceID, byte[].class);
        log.info(String.format("Response status -> %s", response.getStatusCodeValue()));
        return response.getBody();
    }

    public void postSongMetadata(SongDTO dto) {
        ResponseEntity<ResourceDTO> response = MicroserviceUtil.getInstanceRestTemplate().postForEntity(API_GATEWAY_SONG_SERVICE_URL, dto, ResourceDTO.class);
        log.info(String.format("Response status -> %s", response.getStatusCodeValue()));
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

}

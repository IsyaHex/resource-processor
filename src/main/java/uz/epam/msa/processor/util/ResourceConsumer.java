package uz.epam.msa.processor.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uz.epam.msa.processor.Constants;
import uz.epam.msa.processor.dto.SongDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class ResourceConsumer {

    @KafkaListener(topics = "resources_id_topic")
    @Retryable(value = Exception.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000L))
    public void consume(String resourceID) {
        byte[] data = MicroserviceUtil.getInstanceRestTemplate().getForObject(Constants.RESOURCES_SERVICE_URL + resourceID, byte[].class);
        SongDTO dto = createFile(data, resourceID);
        log.info(String.format(Constants.RECEIVED_RESOURCE_ID, resourceID));
        MicroserviceUtil.getInstanceRestTemplate().postForObject(Constants.SONG_SERVICE_URL, dto, SongDTO.class);
        log.info(String.format(Constants.SONG_DTO_FORMED_MSG, dto));
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
//            dto.setYear("2021:02:12-12:59:56");
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

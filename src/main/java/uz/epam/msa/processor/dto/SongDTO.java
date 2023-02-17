package uz.epam.msa.processor.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class SongDTO implements Serializable {
    private String name;
    private String artist;
    private String album;
    private String length;
    private String year;
    private String resourceId;
}

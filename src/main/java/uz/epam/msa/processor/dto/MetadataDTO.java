package uz.epam.msa.processor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataDTO {
    private Integer resourceId;
    private byte[] songMetadata;
}

package uz.epam.msa.processor.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import uz.epam.msa.processor.dto.ResourceDTO;
import uz.epam.msa.processor.dto.SongDTO;

@RestController
public class ProcessorController {

    @Autowired
    private RestTemplate template;

    @PostMapping("/contracts")
    public ResponseEntity<ResourceDTO> saveSongMetadata(@RequestBody SongDTO dto) {
        ResourceDTO response = template.postForObject("http://localhost:1199/songs", dto, ResourceDTO.class);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}


package uz.epam.msa.processor.util;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MicroserviceUtil {

    private static final RestTemplate restTemplate = new RestTemplate();

    @Bean
    public static RestTemplate getInstanceRestTemplate() {
        return restTemplate;
    }
}

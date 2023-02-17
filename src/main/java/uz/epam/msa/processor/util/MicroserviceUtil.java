package uz.epam.msa.processor.util;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MicroserviceUtil {


    @Bean
    public static RestTemplate getInstanceRestTemplate() {
        return new RestTemplate();
    }
}

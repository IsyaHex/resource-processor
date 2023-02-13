package uz.epam.msa.processor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.stereotype.Service;
import uz.epam.msa.processor.bindings.ResourcesListenerBinding;

@Service
@Slf4j
@EnableBinding(ResourcesListenerBinding.class)
public class ProcesserListenerService {
}

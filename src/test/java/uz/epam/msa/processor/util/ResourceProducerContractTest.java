package uz.epam.msa.processor.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.LOCAL,
        ids = "uz.epam.msa.song:song-service:+:stubs:1199")
public class ResourceProducerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void postSongMetadata() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/contracts")
                .content("{\"name\" : \"test\",\"artist\":\"test-artist\",\"album\":\"test-album\",\"length\":\"99999\",\"year\":\"99999\",\"resourceId\":\"999\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}

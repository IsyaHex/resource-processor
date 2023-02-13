package uz.epam.msa.processor.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.springframework.stereotype.Controller;
import uz.epam.msa.processor.constant.Constants;
import uz.epam.msa.processor.dto.MetadataDTO;
import uz.epam.msa.processor.exception.InternalServerErrorException;

import java.io.IOException;
import java.util.Arrays;

@Controller
@Slf4j
public class ProcessorController {
    private final HttpClient client = new HttpClient();

    public byte[] getResourceMetadata(Integer resourceId) {
        GetMethod method = new GetMethod(Constants.RESOURCES_URL + resourceId);
//        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//                new DefaultHttpMethodRetryHandler(3, false));
        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                log.info(String.format(Constants.STATUS_CODE, statusCode));
                throw new InternalServerErrorException();
            }
            return method.getResponseBody();

        } catch (IOException e) {
            throw new InternalServerErrorException();
        }
    }

    public byte[] updateSongMetadata(Integer resourceId, byte[] songMetadata) {
        PostMethod method = new PostMethod(Constants.SONGS_URL);

        NameValuePair[] data = {
                new NameValuePair("resourceId", String.valueOf(resourceId)),
        };
        method.setRequestBody(data);

//        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//                new DefaultHttpMethodRetryHandler(3, false));
        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                throw new InternalServerErrorException();
            }
            return method.getResponseBody();
        } catch (IOException e) {
            throw new InternalServerErrorException();
        }
    }
}

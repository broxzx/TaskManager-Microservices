package com.project.taskservice.utils;

import com.project.taskservice.exceptions.DefaultException;
import com.project.taskservice.exceptions.TokenInvalidException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class UserErrorFeignDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
        HttpStatus responseStatus = HttpStatus.resolve(response.status());
        StringBuilder responseBody = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(response.body().asReader(Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line).append("\n");
            }
        } catch (IOException exception) {
            log.error(exception.getMessage());
        }


        if (responseStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
            return new TokenInvalidException(responseBody.toString());
        } else {
            return new DefaultException(responseBody.toString());
        }
    }

}

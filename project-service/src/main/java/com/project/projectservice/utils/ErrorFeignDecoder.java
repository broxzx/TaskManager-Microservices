package com.project.projectservice.utils;

import com.project.projectservice.exceptions.DefaultException;
import com.project.projectservice.exceptions.TokenNotValidException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class ErrorFeignDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        HttpStatus httpResponseStatus = HttpStatus.resolve(response.status());
        StringBuilder httpResponseBody = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(response.body().asReader(Charset.defaultCharset()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                httpResponseBody.append(line).append("\n");
            }
        } catch (IOException exception) {
            log.error(exception.getMessage());
        }

        if (httpResponseStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
            return new TokenNotValidException(httpResponseBody.toString());
        } else {
            return new DefaultException(httpResponseBody.toString());
        }
    }
}

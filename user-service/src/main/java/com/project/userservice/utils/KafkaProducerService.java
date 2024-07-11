package com.project.userservice.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> producer;

    @Value("${kafka.forgot-password-topic}")
    private String topicForgotPassword;

    public void sendForgotPasswordMail(String userEmail) {
        producer.send(topicForgotPassword, userEmail);
        log.info("reset password mail to email '%s' was sent".formatted(userEmail));
    }

}

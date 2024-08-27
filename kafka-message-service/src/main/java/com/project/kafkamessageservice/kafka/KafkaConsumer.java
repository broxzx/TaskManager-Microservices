package com.project.kafkamessageservice.kafka;

import com.project.kafkamessageservice.utils.MailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final MailSenderService mailSenderService;

    /**
     * listens to topic and when message occurs then sends email
     *
     * @param userEmail represents user's email
     */
    @KafkaListener(topics = {"${kafka.forgot-password-topic}"}, groupId = "message-service")
    public void consume(String userEmail) {
        log.info("Received message: {}", userEmail);
        mailSenderService.sendResetPasswordMail(userEmail);
    }
}

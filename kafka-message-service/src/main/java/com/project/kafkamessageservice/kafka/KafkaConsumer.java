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

    @KafkaListener(topics = {"${kafka.forgot-password-topic}"}, groupId = "message-service")
    public void consume(String userEmail) {
        log.info("Received message: {}", userEmail);
        mailSenderService.sendResetPasswordMail(userEmail);
    }
}

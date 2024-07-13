package com.project.kafkamessageservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = {"${kafka.forgot-password-topic}"}, groupId = "message-service")
    public void consume(String message) {
        System.out.println("Received message: " + message);
    }
}

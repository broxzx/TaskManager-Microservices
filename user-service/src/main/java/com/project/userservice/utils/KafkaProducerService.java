package com.project.userservice.utils;

import com.project.userservice.exception.EntityNotFoundException;
import com.project.userservice.user.service.UserRepository;
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
    private final UserRepository userRepository;

    @Value("${kafka.forgot-password-topic}")
    private String topicForgotPassword;

    /**
     * used for sending 'forgot password' email
     *
     * @param userEmail represents where to send email
     */
    public void sendForgotPasswordMail(String userEmail) {
        if (checkUserExists(userEmail)) {
            producer.send(topicForgotPassword, userEmail);
            log.info("reset password mail to email '%s' was sent".formatted(userEmail));
        } else {
            throw new EntityNotFoundException("User with email '%s' not found".formatted(userEmail));
        }

    }

    private boolean checkUserExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

}

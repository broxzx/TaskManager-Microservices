package com.project.kafkamessageservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailSenderService {

    private final SimpleMailMessage simpleMailMessage;
    private final JwtUtils jwtUtils;
    private final MailSender mailSender;

    @Value("${common.baseUrl}")
    private String baseUrl;

    /**
     * sends 'forgot password' email to user's email
     * @param email represents user's email
     */
    public void sendResetPasswordMail(String email) {
        String resetPasswordUrl = baseUrl + "users/changePassword?token=%s".formatted(jwtUtils.generateResetPasswordToken(email));

        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("Reset your password");
        simpleMailMessage.setText("To reset your password please click on following link: %s".formatted(resetPasswordUrl));

        mailSender.send(simpleMailMessage);
    }

}

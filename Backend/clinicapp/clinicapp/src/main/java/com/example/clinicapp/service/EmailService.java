package com.example.clinicapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender javaMailSender;

    @Autowired(required = false)
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    
    // Default constructor when no mail sender is available
    public EmailService() {
        this.javaMailSender = null;
    }

    public void sendEmail(String to, String subject, String text) {
        if (javaMailSender == null) {
            logger.warn("Email service is disabled. Would have sent email to: {} with subject: {}", to, subject);
            return;
        }
        
        if (to == null || subject == null || text == null) {
            logger.warn("Email not sent - missing parameters. to: {}, subject: {}", to, subject);
            return;
        }
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        javaMailSender.send(message);
    }
}

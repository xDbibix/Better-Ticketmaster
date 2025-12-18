package com.yorku.betterticketmaster.domain.services.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.yorku.betterticketmaster.domain.services.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendEmail(String to, String subject, String template, Map<String, Object> model) {
        // For now simply log to stdout; real SMTP wiring can be added later
        System.out.println("[Email] to=" + to + " subject=" + subject + " template=" + template + " model=" + model);
    }

}

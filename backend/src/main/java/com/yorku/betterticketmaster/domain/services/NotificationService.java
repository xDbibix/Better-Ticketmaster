package com.yorku.betterticketmaster.domain.services;

import java.util.Map;

public interface NotificationService {
    void sendEmail(String to, String subject, String template, Map<String,Object> model);
}

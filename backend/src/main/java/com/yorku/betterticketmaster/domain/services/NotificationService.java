package com.yorku.betterticketmaster.domain.services;

import java.util.Map;

public interface NotificationService {
    /**
     * Send a templated email.
     * @param to recipient address
     * @param subject email subject
     * @param template template name
     * @param model template model (variables)
     */
    void sendEmail(String to, String subject, String template, Map<String,Object> model);
}

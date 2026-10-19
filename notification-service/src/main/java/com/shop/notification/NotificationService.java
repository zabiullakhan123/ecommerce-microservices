package com.shop.notification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Simulates sending email. Swap the log line for JavaMailSender / SES / SendGrid in production. */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int MAX_KEPT = 100;

    public record Notification(String orderId, String to, String subject, String body, Instant sentAt) {
    }

    private final Deque<Notification> recent = new ConcurrentLinkedDeque<>();

    public void send(String orderId, String to, String subject, String body) {
        Notification n = new Notification(orderId, to, subject, body, Instant.now());
        recent.addFirst(n);
        while (recent.size() > MAX_KEPT) {
            recent.pollLast();
        }
        log.info("EMAIL to={} subject=\"{}\" body=\"{}\"", to, subject, body);
    }

    public List<Notification> recent() {
        return new ArrayList<>(recent);
    }
}

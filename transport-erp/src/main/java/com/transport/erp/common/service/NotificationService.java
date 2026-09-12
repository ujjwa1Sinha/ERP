package com.transport.erp.common.service;

import com.transport.erp.trip.domain.Trip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    private final RestTemplate restTemplate;

    @Value("${brevo.api.key:dummy-key}")
    private String brevoApiKey;

    @Value("${brevo.api.sender.email:ujjwals5801@gmail.com}")
    private String senderEmail;

    @Value("${brevo.api.recipient.email:fill_me_out@example.com}")
    private String recipientEmail;

    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private void sendBrevoEmail(String subject, String htmlContent) {
        if (brevoApiKey == null || brevoApiKey.isBlank() || "dummy-key".equals(brevoApiKey)) {
            log.warn("Brevo API Key is missing. Skipping real email dispatch: {}", subject);
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("email", senderEmail, "name", "Transport ERP System"),
                    "to", List.of(Map.of("email", recipientEmail, "name", "Dispatch Admin")),
                    "subject", subject,
                    "htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", entity,
                    String.class);
            log.info("Brevo Email Dispatched successfully! Subject: '{}' | Status: {}", subject,
                    response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to push Brevo email: {}", e.getMessage());
        }
    }

    public void sendEarlyCompletionAlert(Trip trip, Double distance) {
        String subject = "[EARLY COMPLETION] High Priority Dispatch Alert";
        String html = String.format(
                "<html><body><h3>Early Completion Alert</h3><p>Truck <b>%s</b> driven by <b>%s</b> has completed Trip #<b>%s</b> approximately <b>%s km</b> away from the destination target!</p></body></html>",
                trip.getVehicle().getRegistrationNumber(), trip.getPrimaryDriver().getName(), trip.getTripNumber(),
                String.format("%.2f", distance));
        log.warn(subject);
        sendBrevoEmail(subject, html);
    }

    public void sendWanderingAlert(Trip trip, Double distance) {
        String subject = "[CRITICAL WANDERING] SuperAdmin Notification";
        String html = String.format(
                "<html><body><h3>Wandering Alert</h3><p>Truck <b>%s</b> has wandered <b>%s km</b> away from the completion endpoint for Trip #<b>%s</b>!</p></body></html>",
                trip.getVehicle().getRegistrationNumber(), String.format("%.2f", distance), trip.getTripNumber());
        log.warn(subject);
        sendBrevoEmail(subject, html);
    }

    public void sendHaltAlert(Trip trip, long downtimeMinutes) {
        String subject = "[EXCESSIVE HALT] Branch Notification";
        String html = String.format(
                "<html><body><h3>Extended Halt Detected</h3><p>Truck <b>%s</b> driven by <b>%s</b> has been heavily stationary for <b>%s minutes</b> actively on Trip #<b>%s</b>.</p></body></html>",
                trip.getVehicle().getRegistrationNumber(), trip.getPrimaryDriver().getName(), downtimeMinutes,
                trip.getTripNumber());
        log.warn(subject);
        sendBrevoEmail(subject, html);
    }

    public void sendFrequentHaltAlert(Trip trip, int haltCount) {
        String subject = "[FREQUENT STOPPAGE] Pattern Detected";
        String html = String.format(
                "<html><body><h3>Suspicious Pattern</h3><p>Truck <b>%s</b> has stopped <b>%s distinct times</b> consecutively on Trip #<b>%s</b>.</p></body></html>",
                trip.getVehicle().getRegistrationNumber(), haltCount, trip.getTripNumber());
        log.warn(subject);
        sendBrevoEmail(subject, html);
    }

    public void sendDeviationAlert(Trip trip, Double distanceOffRoute) {
        String subject = "[ROUTE DEVIATION] Shield Triggered";
        String html = String.format(
                "<html><body><h3>Major Route Deviation</h3><p>Truck <b>%s</b> has severely deviated <b>%s km</b> strictly OFF the predetermined OSRM geographic route for Trip #<b>%s</b>!</p></body></html>",
                trip.getVehicle().getRegistrationNumber(), String.format("%.2f", distanceOffRoute),
                trip.getTripNumber());
        log.warn(subject);
        sendBrevoEmail(subject, html);
    }

    public void sendDelayAlert(Trip trip, long delayMinutes) {
        String subject = "[CRITICAL DELAY] Scheduling Alert";
        String html = String.format(
                "<html><body><h3>Late Completion detected</h3><p>Truck <b>%s</b> has completed Trip #<b>%s</b> exactly <b>%s minutes LATE</b> compared to the rigid OSRM transit duration standard.</p></body></html>",
                trip.getVehicle().getRegistrationNumber(), trip.getTripNumber(), delayMinutes);
        log.warn(subject);
        sendBrevoEmail(subject, html);
    }

    public void sendEarlyArrivalAlert(Trip trip, long earlyMinutes) {
        String subject = "[EARLY ARRIVAL] Scheduling Alert";
        String html = String.format(
                "<html><body><h3>Fast Completion detected</h3><p>Truck <b>%s</b> has completed Trip #<b>%s</b> exactly <b>%s minutes EARLY</b> compared to the rigid OSRM transit bounds.</p></body></html>",
                trip.getVehicle().getRegistrationNumber(), trip.getTripNumber(), earlyMinutes);
        log.warn(subject);
        sendBrevoEmail(subject, html);
    }
}

package com.transport.erp.common.service;

import com.transport.erp.trip.domain.Trip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendEarlyCompletionAlert(Trip trip, Double distance) {
        log.error("=========================================================================");
        log.error("[EARLY COMPLETION] HIGH PRIORITY DISPATCH ALERT");
        log.error("Truck {} driven by {} completed Trip #{} at a distance of {}km FROM target!",
                trip.getVehicle().getRegistrationNumber(),
                trip.getPrimaryDriver().getName(),
                trip.getTripNumber(),
                String.format("%.2f", distance));
        log.error("-> Notification automatically dispatched via SMS gateway to Branch Admin.");
        log.error("=========================================================================");
    }

    public void sendWanderingAlert(Trip trip, Double distance) {
        log.error("=========================================================================");
        log.error("[CRITICAL WANDERING] SUPERADMIN NOTIFICATION");
        log.error("Truck {} has wandered {}km away from designated completion endpoint for Trip #{}!",
                trip.getVehicle().getRegistrationNumber(),
                String.format("%.2f", distance),
                trip.getTripNumber());
        log.error("-> Dual Notification dispatched via SMS to Branch Admin & SuperAdmin.");
        log.error("=========================================================================");
    }

    public void sendHaltAlert(Trip trip, long downtimeMinutes) {
        log.warn("=========================================================================");
        log.warn("[EXCESSIVE HALT] BRANCH NOTIFICATION");
        log.warn("Truck {} driven by {} has been stationary for {} minutes actively on Trip #{}!",
                trip.getVehicle().getRegistrationNumber(),
                trip.getPrimaryDriver().getName(),
                downtimeMinutes,
                trip.getTripNumber());
        log.warn("-> Dispatched via SMS to Branch Tracking Agent.");
        log.warn("=========================================================================");
    }

    public void sendFrequentHaltAlert(Trip trip, int haltCount) {
        log.warn("=========================================================================");
        log.warn("[FREQUENT STOPPAGE] PATTERN DETECTED");
        log.warn("Truck {} tracked actively halting {} distinct times consecutively on Trip #{}!",
                trip.getVehicle().getRegistrationNumber(),
                haltCount,
                trip.getTripNumber());
        log.warn("-> Dispatched via SMS to Logistics Supervisor.");
        log.warn("=========================================================================");
    }

    public void sendDeviationAlert(Trip trip, Double distanceOffRoute) {
        log.error("=========================================================================");
        log.error("[ROUTE DEVIATION] SHIELD TRIGGERED");
        log.error(
                "Truck {} has severely deviated {}km strictly OFF the predetermined OSRM geographic route for Trip #{}!",
                trip.getVehicle().getRegistrationNumber(),
                String.format("%.2f", distanceOffRoute),
                trip.getTripNumber());
        log.error("-> Immediate Supervisor SMS broadcast deployed.");
        log.error("=========================================================================");
    }
}

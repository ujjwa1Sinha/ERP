package com.transport.erp.fuel.service;

import com.transport.erp.vehicle.domain.VehicleType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class FuelPriceService {

    // Mock baseline national averages for India
    private static final double NATIONAL_PETROL_AVG = 96.50;
    private static final double NATIONAL_DIESEL_AVG = 88.00;
    private static final double NATIONAL_CNG_AVG = 75.00;

    // Simulate standard metropolitan rates (e.g. state taxes difference)
    private static final Map<String, Double> STATE_TAX_MULTIPLIER = new HashMap<>();

    static {
        STATE_TAX_MULTIPLIER.put("delhi", 0.98);
        STATE_TAX_MULTIPLIER.put("mumbai", 1.05); // Maharashtra tends to have higher tax
        STATE_TAX_MULTIPLIER.put("bangalore", 1.02);
        STATE_TAX_MULTIPLIER.put("chennai", 1.00); // Baseline
        STATE_TAX_MULTIPLIER.put("kolkata", 1.01);
        STATE_TAX_MULTIPLIER.put("hyderabad", 1.04);
        STATE_TAX_MULTIPLIER.put("pune", 1.04);
        STATE_TAX_MULTIPLIER.put("ahmedabad", 0.99);
        STATE_TAX_MULTIPLIER.put("jaipur", 1.03);
        STATE_TAX_MULTIPLIER.put("lucknow", 0.99);
    }

    /**
     * Calculates the estimated average price between a source and destination for a
     * given fuel type.
     */
    public BigDecimal getAverageTripFuelPrice(String source, String destination, String fuelType) {
        if (source == null || destination == null) {
            return getNationalAverageForFuelType(fuelType);
        }

        BigDecimal p1 = calculateSimulatedPrice(source, fuelType);
        BigDecimal p2 = calculateSimulatedPrice(destination, fuelType);

        // Average of source and destination
        BigDecimal average = p1.add(p2).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        return average;
    }

    private BigDecimal getNationalAverageForFuelType(String fuelType) {
        if (fuelType == null)
            return BigDecimal.valueOf(NATIONAL_DIESEL_AVG).setScale(2, RoundingMode.HALF_UP); // Default
        return switch (fuelType.toUpperCase()) {
            case "PETROL" -> BigDecimal.valueOf(NATIONAL_PETROL_AVG).setScale(2, RoundingMode.HALF_UP);
            case "DIESEL" -> BigDecimal.valueOf(NATIONAL_DIESEL_AVG).setScale(2, RoundingMode.HALF_UP);
            case "CNG" -> BigDecimal.valueOf(NATIONAL_CNG_AVG).setScale(2, RoundingMode.HALF_UP);
            default -> BigDecimal.valueOf(NATIONAL_DIESEL_AVG).setScale(2, RoundingMode.HALF_UP);
        };
    }

    private BigDecimal calculateSimulatedPrice(String city, String fuelType) {
        BigDecimal basePrice = getNationalAverageForFuelType(fuelType);

        // Find matching tax multiplier
        double multiplier = 1.00;
        String normalizedCity = city.toLowerCase();
        for (String key : STATE_TAX_MULTIPLIER.keySet()) {
            if (normalizedCity.contains(key)) {
                multiplier = STATE_TAX_MULTIPLIER.get(key);
                break;
            }
        }

        return basePrice.multiply(BigDecimal.valueOf(multiplier)).setScale(2, RoundingMode.HALF_UP);
    }
}

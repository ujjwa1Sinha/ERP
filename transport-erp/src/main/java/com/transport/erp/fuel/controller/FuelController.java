package com.transport.erp.fuel.controller;

import com.transport.erp.common.dto.ApiResponse;
import com.transport.erp.common.dto.PagedResponse;
import com.transport.erp.fuel.dto.FuelTransactionRequest;
import com.transport.erp.fuel.dto.FuelTransactionResponse;
import com.transport.erp.fuel.service.FuelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/fuel")
@RequiredArgsConstructor
public class FuelController {

    private final FuelService fuelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FuelTransactionResponse> createFuelTransaction(
            @Valid @RequestBody FuelTransactionRequest request) {
        FuelTransactionResponse response = fuelService.createFuelTransaction(request);
        return ApiResponse.success("Fuel transaction recorded successfully", response);
    }

    @GetMapping
    public ApiResponse<PagedResponse<FuelTransactionResponse>> getAllFuelTransactions(
            @PageableDefault(size = 10, sort = "date", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<FuelTransactionResponse> page = fuelService.getAllFuelTransactions(pageable);
        PagedResponse<FuelTransactionResponse> pagedResponse = PagedResponse.<FuelTransactionResponse>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
        return ApiResponse.success("Fuel transactions retrieved successfully", pagedResponse);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ApiResponse<PagedResponse<FuelTransactionResponse>> getFuelTransactionsByVehicle(
            @PathVariable UUID vehicleId,
            @PageableDefault(size = 10, sort = "date", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<FuelTransactionResponse> page = fuelService.getFuelTransactionsByVehicle(vehicleId, pageable);
        PagedResponse<FuelTransactionResponse> pagedResponse = PagedResponse.<FuelTransactionResponse>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
        return ApiResponse.success("Vehicle fuel transactions retrieved successfully", pagedResponse);
    }

    @GetMapping("/{id}")
    public ApiResponse<FuelTransactionResponse> getFuelTransactionById(@PathVariable UUID id) {
        FuelTransactionResponse response = fuelService.getFuelTransactionById(id);
        return ApiResponse.success("Fuel transaction retrieved successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFuelTransaction(@PathVariable UUID id) {
        fuelService.deleteFuelTransaction(id);
        return ApiResponse.success("Fuel transaction deleted successfully", null);
    }
}

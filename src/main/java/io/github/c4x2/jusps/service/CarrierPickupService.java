package io.github.c4x2.jusps.service;

import io.github.c4x2.jusps.exception.UspsValidationException;
import io.github.c4x2.jusps.http.HttpRequestBuilder;
import io.github.c4x2.jusps.http.HttpTransport;
import io.github.c4x2.jusps.model.pickup.PickupAvailabilityRequest;
import io.github.c4x2.jusps.model.pickup.PickupAvailabilityResponse;
import io.github.c4x2.jusps.model.pickup.PickupResponse;
import io.github.c4x2.jusps.model.pickup.SchedulePickupRequest;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Service client for the USPS Carrier Pickup 3.0 API.
 * Provides carrier pickup eligibility verification, scheduling, inquiry, and cancellation.
 */
@RequiredArgsConstructor
public class CarrierPickupService {

    private static final String AVAILABILITY_PATH = "/pickup/v3/carrier-pickup/availability";
    private static final String PICKUP_PATH = "/pickup/v3/carrier-pickup";
    private static final String PICKUP_ID_PREFIX = "/pickup/v3/carrier-pickup/";

    private final HttpTransport transport;

    /**
     * Checks if carrier pickup is eligible at a specific address.
     */
    public PickupAvailabilityResponse checkAvailability(PickupAvailabilityRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        HttpRequestBuilder builder = HttpRequestBuilder.post(AVAILABILITY_PATH)
                .body(request);

        return transport.execute(builder, PickupAvailabilityResponse.class);
    }

    /**
     * Checks carrier pickup availability asynchronously.
     */
    public CompletableFuture<PickupAvailabilityResponse> checkAvailabilityAsync(PickupAvailabilityRequest request) {
        return CompletableFuture.supplyAsync(() -> checkAvailability(request));
    }

    /**
     * Schedules a carrier pickup on the next regular delivery day.
     */
    public PickupResponse schedulePickup(SchedulePickupRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        HttpRequestBuilder builder = HttpRequestBuilder.post(PICKUP_PATH)
                .body(request);

        return transport.execute(builder, PickupResponse.class);
    }

    /**
     * Schedules a carrier pickup asynchronously.
     */
    public CompletableFuture<PickupResponse> schedulePickupAsync(SchedulePickupRequest request) {
        return CompletableFuture.supplyAsync(() -> schedulePickup(request));
    }

    /**
     * Inquires about an existing scheduled pickup by confirmation number.
     */
    public PickupResponse inquirePickup(String confirmationNumber) {
        if (confirmationNumber == null || confirmationNumber.trim().isEmpty()) {
            throw new UspsValidationException("confirmationNumber cannot be null or blank");
        }
        HttpRequestBuilder builder = HttpRequestBuilder.get(PICKUP_ID_PREFIX + confirmationNumber.trim());
        return transport.execute(builder, PickupResponse.class);
    }

    /**
     * Inquires about a scheduled pickup asynchronously.
     */
    public CompletableFuture<PickupResponse> inquirePickupAsync(String confirmationNumber) {
        return CompletableFuture.supplyAsync(() -> inquirePickup(confirmationNumber));
    }

    /**
     * Cancels an existing scheduled carrier pickup.
     */
    public PickupResponse cancelPickup(String confirmationNumber) {
        if (confirmationNumber == null || confirmationNumber.trim().isEmpty()) {
            throw new UspsValidationException("confirmationNumber cannot be null or blank");
        }
        HttpRequestBuilder builder = HttpRequestBuilder.delete(PICKUP_ID_PREFIX + confirmationNumber.trim());
        return transport.execute(builder, PickupResponse.class);
    }

    /**
     * Cancels an existing carrier pickup asynchronously.
     */
    public CompletableFuture<PickupResponse> cancelPickupAsync(String confirmationNumber) {
        return CompletableFuture.supplyAsync(() -> cancelPickup(confirmationNumber));
    }
}

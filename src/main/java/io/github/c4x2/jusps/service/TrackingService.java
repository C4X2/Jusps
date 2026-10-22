package io.github.c4x2.jusps.service;

import io.github.c4x2.jusps.exception.UspsValidationException;
import io.github.c4x2.jusps.http.HttpRequestBuilder;
import io.github.c4x2.jusps.http.HttpTransport;
import io.github.c4x2.jusps.model.tracking.TrackInfo;
import io.github.c4x2.jusps.model.tracking.TrackingExpand;
import io.github.c4x2.jusps.model.tracking.TrackingResponse;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

/**
 * Service client for the USPS Tracking 3.0 API.
 * Provides real-time tracking status, delivery timestamps, and scan history.
 */
@RequiredArgsConstructor
public class TrackingService {

    private static final String TRACKING_PATH_PREFIX = "/tracking/v3/tracking/";

    private final HttpTransport transport;

    /**
     * Retrieves detailed tracking status and full scan event history for a package.
     *
     * @param trackingNumber USPS tracking number or barcode
     * @return TrackInfo containing status, expected delivery, and scan events
     */
    public TrackInfo getTracking(String trackingNumber) {
        return getTracking(trackingNumber, TrackingExpand.DETAIL);
    }

    /**
     * Retrieves tracking status with the specified expansion level (SUMMARY or DETAIL).
     *
     * @param trackingNumber USPS tracking number or barcode
     * @param expand SUMMARY for high-level status or DETAIL for scan events
     * @return TrackInfo
     */
    public TrackInfo getTracking(String trackingNumber, TrackingExpand expand) {
        TrackingResponse response = getTrackingFull(trackingNumber, expand);
        return response != null ? response.getTrackInfo() : null;
    }

    /**
     * Retrieves full tracking response container for a package.
     */
    public TrackingResponse getTrackingFull(String trackingNumber, TrackingExpand expand) {
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new UspsValidationException("trackingNumber cannot be null or empty");
        }
        TrackingExpand expansion = expand != null ? expand : TrackingExpand.DETAIL;
        HttpRequestBuilder builder = HttpRequestBuilder.get(TRACKING_PATH_PREFIX + trackingNumber.trim())
                .queryParam("expand", expansion.name());

        return transport.execute(builder, TrackingResponse.class);
    }

    /**
     * Retrieves tracking details asynchronously.
     */
    public CompletableFuture<TrackInfo> getTrackingAsync(String trackingNumber) {
        return CompletableFuture.supplyAsync(() -> getTracking(trackingNumber));
    }

    /**
     * Retrieves tracking details asynchronously with specified expansion level.
     */
    public CompletableFuture<TrackInfo> getTrackingAsync(String trackingNumber, TrackingExpand expand) {
        return CompletableFuture.supplyAsync(() -> getTracking(trackingNumber, expand));
    }
}

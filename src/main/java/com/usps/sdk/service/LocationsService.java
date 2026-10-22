package com.usps.sdk.service;

import com.usps.sdk.http.HttpRequestBuilder;
import com.usps.sdk.http.HttpTransport;
import com.usps.sdk.model.locations.DropOffLocationsRequest;
import com.usps.sdk.model.locations.LocationResponse;
import com.usps.sdk.model.locations.PostOfficeLocationsRequest;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Service client for the USPS Locations 3.0 API.
 * Provides facility discovery for post offices and destination entry drop-off locations.
 */
@RequiredArgsConstructor
public class LocationsService {

    private static final String DROPOFF_LOCATIONS_PATH = "/locations/v3/dropoff-locations";
    private static final String POST_OFFICES_PATH = "/locations/v3/post-office-locations";

    private final HttpTransport transport;

    /**
     * Finds drop-off and entry facilities eligible for destination entry rates.
     */
    public LocationResponse getDropOffLocations(DropOffLocationsRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        HttpRequestBuilder builder = HttpRequestBuilder.get(DROPOFF_LOCATIONS_PATH)
                .queryParam("destinationZIPCode", request.getDestinationZIPCode())
                .queryParam("mailClass", request.getMailClass())
                .queryParam("destinationEntryFacilityType", request.getDestinationEntryFacilityType());

        return transport.execute(builder, LocationResponse.class);
    }

    /**
     * Finds drop-off facilities asynchronously.
     */
    public CompletableFuture<LocationResponse> getDropOffLocationsAsync(DropOffLocationsRequest request) {
        return CompletableFuture.supplyAsync(() -> getDropOffLocations(request));
    }

    /**
     * Finds retail Post Office locations within a given radius of a ZIP code.
     */
    public LocationResponse getPostOffices(PostOfficeLocationsRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        HttpRequestBuilder builder = HttpRequestBuilder.get(POST_OFFICES_PATH)
                .queryParam("ZIPCode", request.getZipCode())
                .queryParam("radius", request.getRadius());

        return transport.execute(builder, LocationResponse.class);
    }

    /**
     * Finds retail Post Offices asynchronously.
     */
    public CompletableFuture<LocationResponse> getPostOfficesAsync(PostOfficeLocationsRequest request) {
        return CompletableFuture.supplyAsync(() -> getPostOffices(request));
    }
}

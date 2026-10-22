package com.usps.sdk.service;

import com.usps.sdk.http.HttpRequestBuilder;
import com.usps.sdk.http.HttpTransport;
import com.usps.sdk.model.standards.ServiceStandardRequest;
import com.usps.sdk.model.standards.ServiceStandardResponse;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Service client for the USPS Service Standards 3.0 API.
 * Provides delivery day estimates and benchmarks between origin and destination ZIP codes.
 */
@RequiredArgsConstructor
public class ServiceStandardsService {

    private static final String ESTIMATES_PATH = "/service-standards/v3/estimates";

    private final HttpTransport transport;

    /**
     * Retrieves delivery day estimates for the specified shipment parameters.
     *
     * @param request service standard query parameters
     * @return ServiceStandardResponse containing delivery days and dates
     */
    public ServiceStandardResponse getEstimates(ServiceStandardRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        HttpRequestBuilder builder = HttpRequestBuilder.get(ESTIMATES_PATH)
                .queryParam("originZIPCode", request.getOriginZIPCode())
                .queryParam("destinationZIPCode", request.getDestinationZIPCode())
                .queryParam("acceptanceDate", request.getAcceptanceDate())
                .queryParam("mailClass", request.getMailClass())
                .queryParam("destinationType", request.getDestinationType())
                .queryParam("serviceTypeCodes", request.getServiceTypeCodes());

        return transport.execute(builder, ServiceStandardResponse.class);
    }

    /**
     * Retrieves delivery day estimates asynchronously.
     */
    public CompletableFuture<ServiceStandardResponse> getEstimatesAsync(ServiceStandardRequest request) {
        return CompletableFuture.supplyAsync(() -> getEstimates(request));
    }
}

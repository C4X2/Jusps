package com.usps.sdk.service;

import com.usps.sdk.exception.UspsValidationException;
import com.usps.sdk.http.HttpRequestBuilder;
import com.usps.sdk.http.HttpTransport;
import com.usps.sdk.model.labels.CreateLabelRequest;
import com.usps.sdk.model.labels.LabelResponse;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Service client for the USPS Domestic Labels 3.0 API.
 * Provides shipping label generation, label retrieval/reprint, and label cancellation.
 */
@RequiredArgsConstructor
public class LabelsService {

    private static final String LABEL_PATH = "/labels/v3/label";
    private static final String LABEL_ID_PATH_PREFIX = "/labels/v3/label/";

    private final HttpTransport transport;

    /**
     * Generates a new USPS shipping label and returns the tracking barcode and printable image.
     *
     * @param request label specifications, origin, and destination addresses
     * @return LabelResponse with tracking number and Base64 label data
     */
    public LabelResponse createLabel(CreateLabelRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        HttpRequestBuilder builder = HttpRequestBuilder.post(LABEL_PATH)
                .body(request);

        if (request.getPaymentAuthorizationToken() != null && !request.getPaymentAuthorizationToken().isEmpty()) {
            builder.header("X-Payment-Authorization-Token", request.getPaymentAuthorizationToken());
        }

        return transport.execute(builder, LabelResponse.class);
    }

    /**
     * Generates a new USPS shipping label asynchronously.
     */
    public CompletableFuture<LabelResponse> createLabelAsync(CreateLabelRequest request) {
        return CompletableFuture.supplyAsync(() -> createLabel(request));
    }

    /**
     * Retrieves an existing shipping label by its labelId.
     *
     * @param labelId unique identifier of the label
     * @return LabelResponse
     */
    public LabelResponse getLabel(String labelId) {
        if (labelId == null || labelId.trim().isEmpty()) {
            throw new UspsValidationException("labelId cannot be null or blank");
        }
        HttpRequestBuilder builder = HttpRequestBuilder.get(LABEL_ID_PATH_PREFIX + labelId.trim());
        return transport.execute(builder, LabelResponse.class);
    }

    /**
     * Retrieves an existing shipping label asynchronously.
     */
    public CompletableFuture<LabelResponse> getLabelAsync(String labelId) {
        return CompletableFuture.supplyAsync(() -> getLabel(labelId));
    }

    /**
     * Cancels / voids an unused shipping label.
     *
     * @param labelId unique identifier of the label to cancel
     */
    public void cancelLabel(String labelId) {
        if (labelId == null || labelId.trim().isEmpty()) {
            throw new UspsValidationException("labelId cannot be null or blank");
        }
        HttpRequestBuilder builder = HttpRequestBuilder.delete(LABEL_ID_PATH_PREFIX + labelId.trim());
        transport.execute(builder, Void.class);
    }

    /**
     * Cancels / voids a shipping label asynchronously.
     */
    public CompletableFuture<Void> cancelLabelAsync(String labelId) {
        return CompletableFuture.runAsync(() -> cancelLabel(labelId));
    }
}

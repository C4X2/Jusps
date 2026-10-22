package com.usps.sdk.service;

import com.usps.sdk.http.HttpRequestBuilder;
import com.usps.sdk.http.HttpTransport;
import com.usps.sdk.model.prices.BaseRatesRequest;
import com.usps.sdk.model.prices.RateResponse;
import com.usps.sdk.model.prices.TotalRatesRequest;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Service client for the USPS Domestic Prices 3.0 API.
 * Provides postage rate calculation and price quotes across all mail classes.
 */
@RequiredArgsConstructor
public class PricesService {

    private static final String BASE_RATES_PATH = "/prices/v3/base-rates/search";
    private static final String TOTAL_RATES_PATH = "/prices/v3/total-rates/search";

    private final HttpTransport transport;

    /**
     * Calculates base postage rates for a package with specific mail class and parameters.
     *
     * @param request base rates search criteria
     * @return calculated postage rate quote
     */
    public RateResponse calculateBaseRates(BaseRatesRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        HttpRequestBuilder builder = HttpRequestBuilder.post(BASE_RATES_PATH)
                .body(request);

        return transport.execute(builder, RateResponse.class);
    }

    /**
     * Calculates base postage rates asynchronously.
     */
    public CompletableFuture<RateResponse> calculateBaseRatesAsync(BaseRatesRequest request) {
        return CompletableFuture.supplyAsync(() -> calculateBaseRates(request));
    }

    /**
     * Searches total rates across all eligible USPS mail classes simultaneously.
     *
     * @param request total rates search criteria
     * @return list of eligible rate options with pricing
     */
    public RateResponse searchTotalRates(TotalRatesRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        HttpRequestBuilder builder = HttpRequestBuilder.post(TOTAL_RATES_PATH)
                .body(request);

        return transport.execute(builder, RateResponse.class);
    }

    /**
     * Searches total rates across all eligible mail classes asynchronously.
     */
    public CompletableFuture<RateResponse> searchTotalRatesAsync(TotalRatesRequest request) {
        return CompletableFuture.supplyAsync(() -> searchTotalRates(request));
    }
}

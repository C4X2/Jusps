package com.usps.sdk.service;

import com.usps.sdk.exception.UspsValidationException;
import com.usps.sdk.http.HttpRequestBuilder;
import com.usps.sdk.http.HttpTransport;
import com.usps.sdk.model.address.*;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Service client for the USPS Addresses 3.0 API.
 * Provides address standardization, delivery point validation, city/state lookup, and ZIP code lookup.
 */
@RequiredArgsConstructor
public class AddressesService {

    private static final String ADDRESS_PATH = "/addresses/v3/address";
    private static final String CITY_STATE_PATH = "/addresses/v3/city-state";
    private static final String ZIPCODE_PATH = "/addresses/v3/zipcode";

    private final HttpTransport transport;

    /**
     * Standardizes and validates a domestic US postal address.
     *
     * @param request the address parameters
     * @return standardized address response including DPV metadata
     */
    public AddressResponse standardizeAddress(AddressRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        HttpRequestBuilder builder = HttpRequestBuilder.get(ADDRESS_PATH)
                .queryParam("streetAddress", request.getStreetAddress())
                .queryParam("secondaryAddress", request.getSecondaryAddress())
                .queryParam("city", request.getCity())
                .queryParam("state", request.getState())
                .queryParam("ZIPCode", request.getZipCode())
                .queryParam("ZIPPlus4", request.getZipPlus4())
                .queryParam("firm", request.getFirm());

        return transport.execute(builder, AddressResponse.class);
    }

    /**
     * Standardizes and validates a domestic US postal address asynchronously.
     */
    public CompletableFuture<AddressResponse> standardizeAddressAsync(AddressRequest request) {
        return CompletableFuture.supplyAsync(() -> standardizeAddress(request));
    }

    /**
     * Performs a City/State lookup for a 5-digit ZIP Code.
     *
     * @param zipCode 5-digit US ZIP Code
     * @return official city and state associated with the ZIP Code
     */
    public CityStateResponse lookupCityState(String zipCode) {
        if (zipCode == null || !zipCode.matches("^\\d{5}$")) {
            throw new UspsValidationException("zipCode must be a 5-digit numeric string");
        }
        HttpRequestBuilder builder = HttpRequestBuilder.get(CITY_STATE_PATH)
                .queryParam("ZIPCode", zipCode);

        return transport.execute(builder, CityStateResponse.class);
    }

    /**
     * Performs a City/State lookup asynchronously.
     */
    public CompletableFuture<CityStateResponse> lookupCityStateAsync(String zipCode) {
        return CompletableFuture.supplyAsync(() -> lookupCityState(zipCode));
    }

    /**
     * Finds the 5-digit ZIP Code and ZIP+4 for a given street address, city, and state.
     *
     * @param request the street address, city, and state
     * @return matching ZIP code response
     */
    public ZipCodeLookupResponse lookupZipCode(ZipCodeLookupRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        HttpRequestBuilder builder = HttpRequestBuilder.get(ZIPCODE_PATH)
                .queryParam("streetAddress", request.getStreetAddress())
                .queryParam("secondaryAddress", request.getSecondaryAddress())
                .queryParam("city", request.getCity())
                .queryParam("state", request.getState())
                .queryParam("firm", request.getFirm());

        return transport.execute(builder, ZipCodeLookupResponse.class);
    }

    /**
     * Finds the ZIP Code asynchronously.
     */
    public CompletableFuture<ZipCodeLookupResponse> lookupZipCodeAsync(ZipCodeLookupRequest request) {
        return CompletableFuture.supplyAsync(() -> lookupZipCode(request));
    }
}

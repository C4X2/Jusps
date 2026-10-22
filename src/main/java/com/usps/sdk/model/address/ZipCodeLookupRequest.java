package com.usps.sdk.model.address;

import com.usps.sdk.exception.UspsValidationException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Request parameters for the USPS ZIP Code Lookup API (GET /addresses/v3/zipcode).
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ZipCodeLookupRequest {

    private final String streetAddress;
    private final String secondaryAddress;
    private final String city;
    private final String state;
    private final String firm;

    public static class ZipCodeLookupRequestBuilder {
        public ZipCodeLookupRequest build() {
            if (city == null || city.trim().isEmpty()) {
                throw new UspsValidationException("city is required for ZIP Code lookup");
            }
            if (state == null || state.trim().isEmpty()) {
                throw new UspsValidationException("state is required for ZIP Code lookup");
            }
            return new ZipCodeLookupRequest(streetAddress, secondaryAddress, city, state, firm);
        }
    }
}

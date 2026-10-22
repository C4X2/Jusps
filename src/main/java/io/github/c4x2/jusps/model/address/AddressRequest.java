package io.github.c4x2.jusps.model.address;

import io.github.c4x2.jusps.exception.UspsValidationException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Request parameters for the USPS Address Standardization API (GET /addresses/v3/address).
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AddressRequest {

    private final String streetAddress;
    private final String secondaryAddress;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String zipPlus4;
    private final String firm;

    public static class AddressRequestBuilder {
        public AddressRequest build() {
            if (streetAddress == null || streetAddress.trim().isEmpty()) {
                throw new UspsValidationException("streetAddress is required for address standardization");
            }
            boolean hasCityState = (city != null && !city.trim().isEmpty()) && (state != null && !state.trim().isEmpty());
            boolean hasZip = (zipCode != null && !zipCode.trim().isEmpty());
            if (!hasCityState && !hasZip) {
                throw new UspsValidationException("Either city and state or a ZIPCode must be provided alongside streetAddress");
            }
            return new AddressRequest(streetAddress, secondaryAddress, city, state, zipCode, zipPlus4, firm);
        }
    }
}

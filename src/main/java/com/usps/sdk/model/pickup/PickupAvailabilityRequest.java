package com.usps.sdk.model.pickup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.usps.sdk.exception.UspsValidationException;
import com.usps.sdk.model.common.Address;
import lombok.*;

/**
 * Request payload to verify pickup eligibility for an address (POST /pickup/v3/carrier-pickup/availability).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PickupAvailabilityRequest {

    @JsonProperty("pickupAddress")
    private Address pickupAddress;

    public static class PickupAvailabilityRequestBuilder {
        public PickupAvailabilityRequest build() {
            if (pickupAddress == null) {
                throw new UspsValidationException("pickupAddress is required");
            }
            return new PickupAvailabilityRequest(pickupAddress);
        }
    }
}

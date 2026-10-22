package com.usps.sdk.model.pickup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Result indicating whether carrier pickup is available for the given address.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PickupAvailabilityResponse {

    @JsonProperty("available")
    private Boolean available;

    @JsonProperty("message")
    private String message;

    public boolean isAvailable() {
        return Boolean.TRUE.equals(available);
    }

    @Override
    public String toString() {
        return "PickupAvailabilityResponse{available=" + available + ", message='" + message + "'}";
    }
}

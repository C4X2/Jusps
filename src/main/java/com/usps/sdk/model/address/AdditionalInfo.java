package com.usps.sdk.model.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Delivery Point Validation (DPV) metadata and additional information returned by USPS.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalInfo {

    @JsonProperty("deliveryPoint")
    private String deliveryPoint;

    @JsonProperty("carrierRoute")
    private String carrierRoute;

    @JsonProperty("DPVConfirmation")
    private String dpvConfirmation;

    @JsonProperty("DPVCMRA")
    private String dpvCmra;

    @JsonProperty("business")
    private String business;

    @JsonProperty("centralDeliveryPoint")
    private String centralDeliveryPoint;

    @JsonProperty("vacant")
    private String vacant;

    /**
     * Checks if the address is confirmed as deliverable by USPS (DPV 'Y').
     */
    public boolean isDeliverable() {
        return "Y".equalsIgnoreCase(dpvConfirmation);
    }

    /**
     * Checks if the address was matched but is missing a secondary unit (apartment, suite, etc.) (DPV 'D').
     */
    public boolean isMissingSecondaryUnit() {
        return "D".equalsIgnoreCase(dpvConfirmation);
    }

    /**
     * Checks if the secondary unit was invalid or unmatched (DPV 'S').
     */
    public boolean isInvalidSecondaryUnit() {
        return "S".equalsIgnoreCase(dpvConfirmation);
    }

    /**
     * Checks if the address is a commercial mail receiving agency (CMRA).
     */
    public boolean isCmra() {
        return "Y".equalsIgnoreCase(dpvCmra);
    }

    /**
     * Checks if the address is a business location.
     */
    public boolean isBusinessAddress() {
        return "Y".equalsIgnoreCase(business);
    }

    /**
     * Checks if the address has been flagged as vacant.
     */
    public boolean isVacantAddress() {
        return "Y".equalsIgnoreCase(vacant);
    }

    @Override
    public String toString() {
        return "AdditionalInfo{" +
                "dpvConfirmation='" + dpvConfirmation + '\'' +
                ", carrierRoute='" + carrierRoute + '\'' +
                ", business='" + business + '\'' +
                ", vacant='" + vacant + '\'' +
                '}';
    }
}

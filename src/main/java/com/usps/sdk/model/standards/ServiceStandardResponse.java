package com.usps.sdk.model.standards;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the delivery standards response returned by USPS.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceStandardResponse {

    @JsonProperty("estimates")
    @JsonAlias({"serviceStandards", "standards"})
    private List<ServiceStandardEstimate> estimates;

    public List<ServiceStandardEstimate> getEstimates() {
        return estimates != null ? estimates : Collections.emptyList();
    }

    @Override
    public String toString() {
        return "ServiceStandardResponse{estimatesCount=" + getEstimates().size() + "}";
    }
}

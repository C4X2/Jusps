package io.github.c4x2.jusps.model.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.c4x2.jusps.model.common.Address;
import lombok.*;

import java.util.Collections;
import java.util.List;

/**
 * Standard response returned by the USPS Address Standardization API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressResponse {

    @JsonProperty("firm")
    private String firm;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("additionalInfo")
    private AdditionalInfo additionalInfo;

    @JsonProperty("corrections")
    private List<Correction> corrections;

    @JsonProperty("matches")
    private List<Match> matches;

    public List<Correction> getCorrections() {
        return corrections != null ? corrections : Collections.emptyList();
    }

    public List<Match> getMatches() {
        return matches != null ? matches : Collections.emptyList();
    }

    /**
     * Checks if the address was found and confirmed deliverable.
     */
    public boolean isValid() {
        return address != null && additionalInfo != null && additionalInfo.isDeliverable();
    }

    @Override
    public String toString() {
        return "AddressResponse{" +
                "address=" + address +
                ", additionalInfo=" + additionalInfo +
                ", corrections=" + getCorrections() +
                '}';
    }
}

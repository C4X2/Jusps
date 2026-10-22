package io.github.c4x2.jusps.model.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.c4x2.jusps.model.common.Address;
import lombok.*;

import java.util.Collections;
import java.util.List;

/**
 * Response returned by the USPS ZIP Code Lookup API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZipCodeLookupResponse {

    @JsonProperty("firm")
    private String firm;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("additionalInfo")
    private AdditionalInfo additionalInfo;

    @JsonProperty("corrections")
    private List<Correction> corrections;

    public List<Correction> getCorrections() {
        return corrections != null ? corrections : Collections.emptyList();
    }

    @Override
    public String toString() {
        return "ZipCodeLookupResponse{" +
                "address=" + address +
                ", additionalInfo=" + additionalInfo +
                '}';
    }
}

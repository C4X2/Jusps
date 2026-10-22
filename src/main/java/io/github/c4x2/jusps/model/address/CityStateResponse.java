package io.github.c4x2.jusps.model.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response returned by the USPS City/State Lookup API (GET /addresses/v3/city-state).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityStateResponse {

    @JsonProperty("ZIPCode")
    private String zipCode;

    @JsonProperty("city")
    private String city;

    @JsonProperty("cityAbbreviation")
    private String cityAbbreviation;

    @JsonProperty("state")
    private String state;
}

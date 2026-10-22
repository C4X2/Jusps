package io.github.c4x2.jusps.model.locations;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the location list response returned by USPS Locations API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationResponse {

    @JsonProperty("locations")
    @JsonAlias({"facilities", "dropOffLocations", "postOffices"})
    private List<LocationFacility> locations;

    public List<LocationFacility> getLocations() {
        return locations != null ? locations : Collections.emptyList();
    }

    @Override
    public String toString() {
        return "LocationResponse{locationsCount=" + getLocations().size() + "}";
    }
}

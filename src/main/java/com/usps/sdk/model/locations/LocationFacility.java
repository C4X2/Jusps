package com.usps.sdk.model.locations;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.usps.sdk.model.common.Address;
import lombok.*;

import java.util.Collections;
import java.util.List;

/**
 * Details of a USPS postal facility or drop-off location.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationFacility {

    @JsonProperty("name")
    @JsonAlias({"facilityName", "postOfficeName"})
    private String name;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("phone")
    @JsonAlias({"phoneNumber"})
    private String phone;

    @JsonProperty("facilityType")
    private String facilityType;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("services")
    private List<String> services;

    public List<String> getServices() {
        return services != null ? services : Collections.emptyList();
    }

    @Override
    public String toString() {
        return "LocationFacility{" +
                "name='" + name + '\'' +
                ", address=" + address +
                ", phone='" + phone + '\'' +
                '}';
    }
}

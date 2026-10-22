package io.github.c4x2.jusps.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Common postal address representation used across USPS APIs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("firm")
    private String firm;

    @JsonProperty("streetAddress")
    private String streetAddress;

    @JsonProperty("streetAddressAbbreviation")
    private String streetAddressAbbreviation;

    @JsonProperty("secondaryAddress")
    private String secondaryAddress;

    @JsonProperty("city")
    private String city;

    @JsonProperty("cityAbbreviation")
    private String cityAbbreviation;

    @JsonProperty("state")
    private String state;

    @JsonProperty("ZIPCode")
    private String zipCode;

    @JsonProperty("ZIPPlus4")
    private String zipPlus4;

    @JsonProperty("urbanization")
    private String urbanization;

    @JsonProperty("country")
    private String country;

    @JsonProperty("countryISOCode")
    private String countryISOCode;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    /**
     * Helper to return formatted full ZIP code (e.g. "90210-1234" or "90210").
     */
    public String getFullZip() {
        if (zipCode != null && zipPlus4 != null && !zipPlus4.isEmpty()) {
            return zipCode + "-" + zipPlus4;
        }
        return zipCode;
    }

    @Override
    public String toString() {
        return "Address{" +
                "streetAddress='" + streetAddress + '\'' +
                ", secondaryAddress='" + secondaryAddress + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zipCode='" + getFullZip() + '\'' +
                '}';
    }
}

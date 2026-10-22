package com.usps.sdk.service;

import com.usps.sdk.UspsClient;
import com.usps.sdk.config.UspsEnvironment;
import com.usps.sdk.http.MockHttpClient;
import com.usps.sdk.model.common.MailClass;
import com.usps.sdk.model.locations.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationsServiceTest {

    private MockHttpClient mockHttpClient;
    private UspsClient client;

    @BeforeEach
    void setUp() {
        mockHttpClient = new MockHttpClient();
        client = UspsClient.builder()
                .clientId("TEST_KEY")
                .clientSecret("TEST_SECRET")
                .environment(UspsEnvironment.TESTING)
                .httpClient(mockHttpClient)
                .buildClient();

        mockHttpClient.onRoute("/oauth2/v3/token", req ->
                MockHttpClient.MockResponseSpec.of(200, "{\"access_token\":\"mock_token\",\"expires_in\":3600}"));
    }

    @Test
    void shouldFindDropOffLocations() {
        String locationsJson = """
                {
                  "locations": [
                    {
                      "name": "ATLANTA NDC",
                      "facilityType": "NETWORK_DISTRIBUTION_CENTER",
                      "phone": "404-555-0100",
                      "address": {
                        "streetAddress": "3400 FORREST PARK RD SE",
                        "city": "ATLANTA",
                        "state": "GA",
                        "ZIPCode": "30343"
                      }
                    }
                  ]
                }
                """;

        mockHttpClient.onRoute("/locations/v3/dropoff-locations", req ->
                MockHttpClient.MockResponseSpec.of(200, locationsJson));

        DropOffLocationsRequest request = DropOffLocationsRequest.builder()
                .destinationZIPCode("30343")
                .mailClass(MailClass.PARCEL_SELECT)
                .build();

        LocationResponse response = client.locations().getDropOffLocations(request);

        assertThat(response).isNotNull();
        assertThat(response.getLocations()).hasSize(1);
        LocationFacility facility = response.getLocations().get(0);
        assertThat(facility.getName()).isEqualTo("ATLANTA NDC");
        assertThat(facility.getAddress().getCity()).isEqualTo("ATLANTA");
    }

    @Test
    void shouldFindPostOffices() {
        String poJson = """
                {
                  "locations": [
                    {
                      "name": "MANHATTAN POST OFFICE",
                      "facilityType": "POST_OFFICE",
                      "address": {
                        "streetAddress": "421 8TH AVE",
                        "city": "NEW YORK",
                        "state": "NY",
                        "ZIPCode": "10001"
                      }
                    }
                  ]
                }
                """;

        mockHttpClient.onRoute("/locations/v3/post-office-locations", req ->
                MockHttpClient.MockResponseSpec.of(200, poJson));

        PostOfficeLocationsRequest request = PostOfficeLocationsRequest.builder()
                .zipCode("10001")
                .radius(5)
                .build();

        LocationResponse response = client.locations().getPostOffices(request);

        assertThat(response).isNotNull();
        assertThat(response.getLocations()).hasSize(1);
        assertThat(response.getLocations().get(0).getName()).isEqualTo("MANHATTAN POST OFFICE");
    }
}

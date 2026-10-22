package io.github.c4x2.jusps.service;

import io.github.c4x2.jusps.UspsClient;
import io.github.c4x2.jusps.config.UspsEnvironment;
import io.github.c4x2.jusps.exception.UspsValidationException;
import io.github.c4x2.jusps.http.MockHttpClient;
import io.github.c4x2.jusps.model.address.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressesServiceTest {

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

        // Preset OAuth token endpoint route
        mockHttpClient.onRoute("/oauth2/v3/token", req ->
                MockHttpClient.MockResponseSpec.of(200, "{\"access_token\":\"mock_token\",\"expires_in\":3600}"));
    }

    @Test
    void shouldStandardizeAddressSuccessfully() {
        String addressJson = """
                {
                  "firm": "USPS HEADQUARTERS",
                  "address": {
                    "streetAddress": "475 LENFANT PLZ SW",
                    "streetAddressAbbreviation": "475 LENFANT PLZ SW",
                    "city": "WASHINGTON",
                    "cityAbbreviation": "WASH",
                    "state": "DC",
                    "ZIPCode": "20260",
                    "ZIPPlus4": "0004"
                  },
                  "additionalInfo": {
                    "deliveryPoint": "75",
                    "carrierRoute": "C000",
                    "DPVConfirmation": "Y",
                    "DPVCMRA": "N",
                    "business": "Y",
                    "vacant": "N"
                  },
                  "matches": [
                    { "code": "31", "text": "Single Response - exact match" }
                  ]
                }
                """;

        mockHttpClient.onRoute("/addresses/v3/address", req ->
                MockHttpClient.MockResponseSpec.of(200, addressJson));

        AddressRequest request = AddressRequest.builder()
                .streetAddress("475 L'Enfant Plaza SW")
                .city("Washington")
                .state("DC")
                .zipCode("20260")
                .firm("USPS Headquarters")
                .build();

        AddressResponse response = client.addresses().standardizeAddress(request);

        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat(response.getAddress().getStreetAddress()).isEqualTo("475 LENFANT PLZ SW");
        assertThat(response.getAddress().getCity()).isEqualTo("WASHINGTON");
        assertThat(response.getAddress().getState()).isEqualTo("DC");
        assertThat(response.getAddress().getZipCode()).isEqualTo("20260");
        assertThat(response.getAddress().getZipPlus4()).isEqualTo("0004");
        assertThat(response.getAddress().getFullZip()).isEqualTo("20260-0004");

        assertThat(response.getAdditionalInfo()).isNotNull();
        assertThat(response.getAdditionalInfo().isDeliverable()).isTrue();
        assertThat(response.getAdditionalInfo().isBusinessAddress()).isTrue();
        assertThat(response.getAdditionalInfo().isVacantAddress()).isFalse();
        assertThat(response.getAdditionalInfo().isMissingSecondaryUnit()).isFalse();

        var recorded = mockHttpClient.getRecordedRequests().stream()
                .filter(r -> r.uri().getPath().contains("/addresses/v3/address"))
                .findFirst().orElseThrow();
        assertThat(recorded.headers().firstValue("Authorization")).contains("Bearer mock_token");
        assertThat(recorded.uri().getQuery()).contains("streetAddress=475+L'Enfant+Plaza+SW");
    }

    @Test
    void shouldLookupCityState() {
        String cityStateJson = """
                {
                  "ZIPCode": "90210",
                  "city": "BEVERLY HILLS",
                  "cityAbbreviation": "BEV HILLS",
                  "state": "CA"
                }
                """;
        mockHttpClient.onRoute("/addresses/v3/city-state", req ->
                MockHttpClient.MockResponseSpec.of(200, cityStateJson));

        CityStateResponse response = client.addresses().lookupCityState("90210");

        assertThat(response).isNotNull();
        assertThat(response.getZipCode()).isEqualTo("90210");
        assertThat(response.getCity()).isEqualTo("BEVERLY HILLS");
        assertThat(response.getState()).isEqualTo("CA");
    }

    @Test
    void shouldValidateCityStateZipCodeParameter() {
        assertThatThrownBy(() -> client.addresses().lookupCityState("123"))
                .isInstanceOf(UspsValidationException.class)
                .hasMessageContaining("5-digit numeric string");
    }

    @Test
    void shouldLookupZipCode() {
        String zipLookupJson = """
                {
                  "address": {
                    "streetAddress": "1600 PENNSYLVANIA AVE NW",
                    "city": "WASHINGTON",
                    "state": "DC",
                    "ZIPCode": "20500",
                    "ZIPPlus4": "0003"
                  }
                }
                """;
        mockHttpClient.onRoute("/addresses/v3/zipcode", req ->
                MockHttpClient.MockResponseSpec.of(200, zipLookupJson));

        ZipCodeLookupRequest request = ZipCodeLookupRequest.builder()
                .streetAddress("1600 Pennsylvania Ave NW")
                .city("Washington")
                .state("DC")
                .build();

        ZipCodeLookupResponse response = client.addresses().lookupZipCode(request);

        assertThat(response).isNotNull();
        assertThat(response.getAddress().getZipCode()).isEqualTo("20500");
        assertThat(response.getAddress().getZipPlus4()).isEqualTo("0003");
    }

    @Test
    void shouldValidateAddressRequestParameters() {
        // Missing street address
        assertThatThrownBy(() -> AddressRequest.builder()
                .city("Chicago")
                .state("IL")
                .build())
                .isInstanceOf(UspsValidationException.class)
                .hasMessageContaining("streetAddress is required");

        // Missing both city/state and zip
        assertThatThrownBy(() -> AddressRequest.builder()
                .streetAddress("123 Main St")
                .build())
                .isInstanceOf(UspsValidationException.class)
                .hasMessageContaining("Either city and state or a ZIPCode must be provided");
    }

    @Test
    void shouldSupportAsyncAddressStandardization() throws Exception {
        mockHttpClient.onRoute("/addresses/v3/address", req ->
                MockHttpClient.MockResponseSpec.of(200, "{\"address\":{\"city\":\"SEATTLE\",\"state\":\"WA\",\"ZIPCode\":\"98101\"}}"));

        AddressRequest request = AddressRequest.builder()
                .streetAddress("100 Pine St")
                .city("Seattle")
                .state("WA")
                .build();

        CompletableFuture<AddressResponse> future = client.addresses().standardizeAddressAsync(request);
        AddressResponse response = future.get();

        assertThat(response.getAddress().getCity()).isEqualTo("SEATTLE");
        assertThat(response.getAddress().getState()).isEqualTo("WA");
    }
}

package com.usps.sdk.service;

import com.usps.sdk.UspsClient;
import com.usps.sdk.config.UspsEnvironment;
import com.usps.sdk.http.MockHttpClient;
import com.usps.sdk.model.common.Address;
import com.usps.sdk.model.pickup.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarrierPickupServiceTest {

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
    void shouldCheckPickupAvailability() {
        mockHttpClient.onRoute("/pickup/v3/carrier-pickup/availability", req ->
                MockHttpClient.MockResponseSpec.of(200, "{\"available\":true,\"message\":\"Carrier pickup available\"}"));

        Address address = Address.builder()
                .streetAddress("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .build();

        PickupAvailabilityResponse response = client.carrierPickup().checkAvailability(new PickupAvailabilityRequest(address));

        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    void shouldSchedulePickup() {
        String scheduleJson = """
                {
                  "confirmationNumber": "WEC123456789",
                  "status": "Scheduled",
                  "pickupDate": "2026-10-23",
                  "packageCount": 3
                }
                """;
        mockHttpClient.onRoute("/pickup/v3/carrier-pickup", req ->
                MockHttpClient.MockResponseSpec.of(200, scheduleJson));

        SchedulePickupRequest request = SchedulePickupRequest.builder()
                .pickupAddress(Address.builder().streetAddress("123 Main St").city("Miami").state("FL").zipCode("33101").build())
                .pickupDate("2026-10-23")
                .packageLocation("Front Porch")
                .packageCount(PickupPackageCount.builder()
                        .priorityMailCount(2)
                        .groundAdvantageCount(1)
                        .build())
                .specialInstructions("Ring bell upon arrival")
                .build();

        PickupResponse response = client.carrierPickup().schedulePickup(request);

        assertThat(response).isNotNull();
        assertThat(response.getConfirmationNumber()).isEqualTo("WEC123456789");
        assertThat(response.getStatus()).isEqualTo("Scheduled");
        assertThat(response.getPackageCount()).isEqualTo(3);
    }

    @Test
    void shouldInquireAndCancelPickup() {
        mockHttpClient.onRoute("/pickup/v3/carrier-pickup/WEC123", req -> {
            if ("DELETE".equalsIgnoreCase(req.method())) {
                return MockHttpClient.MockResponseSpec.of(200, "{\"confirmationNumber\":\"WEC123\",\"status\":\"Cancelled\"}");
            }
            return MockHttpClient.MockResponseSpec.of(200, "{\"confirmationNumber\":\"WEC123\",\"status\":\"Scheduled\"}");
        });

        PickupResponse inquiry = client.carrierPickup().inquirePickup("WEC123");
        assertThat(inquiry.getStatus()).isEqualTo("Scheduled");

        PickupResponse cancellation = client.carrierPickup().cancelPickup("WEC123");
        assertThat(cancellation.getStatus()).isEqualTo("Cancelled");
    }
}

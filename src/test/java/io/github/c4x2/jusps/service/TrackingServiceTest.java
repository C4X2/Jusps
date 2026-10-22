package io.github.c4x2.jusps.service;

import io.github.c4x2.jusps.UspsClient;
import io.github.c4x2.jusps.config.UspsEnvironment;
import io.github.c4x2.jusps.exception.UspsNotFoundException;
import io.github.c4x2.jusps.exception.UspsValidationException;
import io.github.c4x2.jusps.http.MockHttpClient;
import io.github.c4x2.jusps.model.tracking.TrackInfo;
import io.github.c4x2.jusps.model.tracking.TrackingExpand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrackingServiceTest {

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
    void shouldTrackPackageWithFullDetails() {
        String trackingJson = """
                {
                  "trackInfo": [
                    {
                      "trackingNumber": "9400100000000000000000",
                      "trackSummary": "Your item was delivered in or at the mailbox at 2:15 pm on October 21 in MIAMI, FL 33101.",
                      "statusCategory": "Delivered",
                      "expectedDeliveryDate": "2026-10-21T18:00:00Z",
                      "actualDeliveryDate": "2026-10-21T14:15:00Z",
                      "destinationCity": "MIAMI",
                      "destinationState": "FL",
                      "destinationZIPCode": "33101",
                      "classOfMail": "PRIORITY_MAIL",
                      "trackDetail": [
                        {
                          "eventTimestamp": "2026-10-21 14:15:00",
                          "eventType": "01",
                          "eventDescription": "Delivered, In/At Mailbox",
                          "eventCity": "MIAMI",
                          "eventState": "FL",
                          "eventZIPCode": "33101"
                        },
                        {
                          "eventTimestamp": "2026-10-21 08:30:00",
                          "eventType": "07",
                          "eventDescription": "Out for Delivery",
                          "eventCity": "MIAMI",
                          "eventState": "FL",
                          "eventZIPCode": "33101"
                        },
                        {
                          "eventTimestamp": "2026-10-20 22:10:00",
                          "eventType": "10",
                          "eventDescription": "Arrived at USPS Regional Facility",
                          "eventCity": "OPA LOCKA",
                          "eventState": "FL",
                          "eventZIPCode": "33054"
                        }
                      ]
                    }
                  ]
                }
                """;

        mockHttpClient.onRoute("/tracking/v3/tracking/9400100000000000000000", req ->
                MockHttpClient.MockResponseSpec.of(200, trackingJson));

        TrackInfo trackInfo = client.tracking().getTracking("9400100000000000000000");

        assertThat(trackInfo).isNotNull();
        assertThat(trackInfo.getTrackingNumber()).isEqualTo("9400100000000000000000");
        assertThat(trackInfo.getStatusCategory()).isEqualTo("Delivered");
        assertThat(trackInfo.isDelivered()).isTrue();
        assertThat(trackInfo.isInTransit()).isFalse();
        assertThat(trackInfo.getActualDeliveryDate()).isEqualTo("2026-10-21T14:15:00Z");
        assertThat(trackInfo.getDestinationCity()).isEqualTo("MIAMI");
        assertThat(trackInfo.getDestinationState()).isEqualTo("FL");
        assertThat(trackInfo.getDestinationZipCode()).isEqualTo("33101");

        assertThat(trackInfo.getTrackDetail()).hasSize(3);
        var latest = trackInfo.getLatestEvent();
        assertThat(latest).isNotNull();
        assertThat(latest.getEventDescription()).isEqualTo("Delivered, In/At Mailbox");
        assertThat(latest.getEventCity()).isEqualTo("MIAMI");
        assertThat(latest.getEventZipCode()).isEqualTo("33101");
    }

    @Test
    void shouldTrackWithSummaryExpansion() {
        mockHttpClient.onRoute("/tracking/v3/tracking/9400100000000000000000", req ->
                MockHttpClient.MockResponseSpec.of(200, "{\"trackInfo\":{\"trackingNumber\":\"9400100000000000000000\",\"statusCategory\":\"In-Transit\"}}"));

        TrackInfo trackInfo = client.tracking().getTracking("9400100000000000000000", TrackingExpand.SUMMARY);

        assertThat(trackInfo).isNotNull();
        assertThat(trackInfo.isInTransit()).isTrue();

        var request = mockHttpClient.getLastRequest();
        assertThat(request.uri().getQuery()).contains("expand=SUMMARY");
    }

    @Test
    void shouldHandleTrackingNotFound() {
        String errorJson = """
                {
                  "apiVersion": "/tracking/v3",
                  "error": {
                    "code": "404",
                    "message": "Tracking number not found"
                  }
                }
                """;
        mockHttpClient.onRoute("/tracking/v3/tracking/0000000000", req ->
                MockHttpClient.MockResponseSpec.of(404, errorJson));

        assertThatThrownBy(() -> client.tracking().getTracking("0000000000"))
                .isInstanceOf(UspsNotFoundException.class)
                .hasMessageContaining("Tracking number not found");
    }

    @Test
    void shouldValidateTrackingNumber() {
        assertThatThrownBy(() -> client.tracking().getTracking(""))
                .isInstanceOf(UspsValidationException.class)
                .hasMessageContaining("trackingNumber cannot be null or empty");
    }

    @Test
    void shouldTrackPackageAsynchronously() throws Exception {
        mockHttpClient.onRoute("/tracking/v3/tracking/9400", req ->
                MockHttpClient.MockResponseSpec.of(200, "{\"trackInfo\":{\"trackingNumber\":\"9400\",\"statusCategory\":\"Pre-Shipment\"}}"));

        CompletableFuture<TrackInfo> future = client.tracking().getTrackingAsync("9400");
        TrackInfo trackInfo = future.get();

        assertThat(trackInfo.isPreShipment()).isTrue();
    }
}

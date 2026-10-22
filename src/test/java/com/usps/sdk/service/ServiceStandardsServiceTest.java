package com.usps.sdk.service;

import com.usps.sdk.UspsClient;
import com.usps.sdk.config.UspsEnvironment;
import com.usps.sdk.http.MockHttpClient;
import com.usps.sdk.model.standards.ServiceStandardEstimate;
import com.usps.sdk.model.standards.ServiceStandardRequest;
import com.usps.sdk.model.standards.ServiceStandardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceStandardsServiceTest {

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
    void shouldGetDeliveryEstimates() {
        String estimatesJson = """
                {
                  "estimates": [
                    {
                      "mailClass": "PRIORITY_MAIL",
                      "serviceTypeDescription": "Priority Mail 2-Day",
                      "deliveryDays": 2,
                      "effectiveAcceptanceDate": "2026-10-22",
                      "estimatedDeliveryDate": "2026-10-24"
                    },
                    {
                      "mailClass": "PRIORITY_MAIL_EXPRESS",
                      "serviceTypeDescription": "Priority Mail Express 1-Day",
                      "deliveryDays": 1,
                      "effectiveAcceptanceDate": "2026-10-22",
                      "estimatedDeliveryDate": "2026-10-23"
                    }
                  ]
                }
                """;

        mockHttpClient.onRoute("/service-standards/v3/estimates", req ->
                MockHttpClient.MockResponseSpec.of(200, estimatesJson));

        ServiceStandardRequest request = ServiceStandardRequest.builder()
                .originZIPCode("10018")
                .destinationZIPCode("95823")
                .acceptanceDate("2026-10-22")
                .build();

        ServiceStandardResponse response = client.serviceStandards().getEstimates(request);

        assertThat(response).isNotNull();
        assertThat(response.getEstimates()).hasSize(2);

        ServiceStandardEstimate first = response.getEstimates().get(0);
        assertThat(first.getMailClass()).isEqualTo("PRIORITY_MAIL");
        assertThat(first.getDeliveryDays()).isEqualTo(2);
        assertThat(first.getEstimatedDeliveryDate()).isEqualTo("2026-10-24");

        var recorded = mockHttpClient.getLastRequest();
        assertThat(recorded.uri().getQuery()).contains("originZIPCode=10018");
        assertThat(recorded.uri().getQuery()).contains("destinationZIPCode=95823");
    }
}

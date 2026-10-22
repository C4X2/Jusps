package io.github.c4x2.jusps.service;

import io.github.c4x2.jusps.UspsClient;
import io.github.c4x2.jusps.config.UspsEnvironment;
import io.github.c4x2.jusps.exception.UspsValidationException;
import io.github.c4x2.jusps.http.MockHttpClient;
import io.github.c4x2.jusps.model.common.MailClass;
import io.github.c4x2.jusps.model.common.PriceType;
import io.github.c4x2.jusps.model.prices.BaseRatesRequest;
import io.github.c4x2.jusps.model.prices.RateOption;
import io.github.c4x2.jusps.model.prices.RateResponse;
import io.github.c4x2.jusps.model.prices.TotalRatesRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricesServiceTest {

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
    void shouldCalculateBaseRates() {
        String baseRateJson = """
                {
                  "totalBasePrice": 9.85,
                  "zone": "4",
                  "rates": [
                    {
                      "mailClass": "PRIORITY_MAIL",
                      "mailClassDescription": "Priority Mail 2-Day",
                      "priceType": "RETAIL",
                      "price": 9.85,
                      "zone": "4"
                    }
                  ]
                }
                """;
        mockHttpClient.onRoute("/prices/v3/base-rates/search", req ->
                MockHttpClient.MockResponseSpec.of(200, baseRateJson));

        BaseRatesRequest request = BaseRatesRequest.builder()
                .originZIPCode("90210")
                .destinationZIPCode("10001")
                .weight(2.5)
                .dimensions(10.0, 5.0, 4.0)
                .mailClass(MailClass.PRIORITY_MAIL)
                .priceType(PriceType.RETAIL)
                .build();

        RateResponse response = client.prices().calculateBaseRates(request);

        assertThat(response).isNotNull();
        assertThat(response.getTotalBasePrice()).isEqualByComparingTo(new BigDecimal("9.85"));
        assertThat(response.getZone()).isEqualTo("4");

        RateOption primary = response.getPrimaryRate();
        assertThat(primary).isNotNull();
        assertThat(primary.getMailClass()).isEqualTo("PRIORITY_MAIL");
        assertThat(primary.getPrice()).isEqualByComparingTo(new BigDecimal("9.85"));

        var recordedBody = mockHttpClient.getLastRequestBody();
        assertThat(recordedBody).contains("\"originZIPCode\":\"90210\"");
        assertThat(recordedBody).contains("\"destinationZIPCode\":\"10001\"");
        assertThat(recordedBody).contains("\"weight\":2.5");
        assertThat(recordedBody).contains("\"mailClass\":\"PRIORITY_MAIL\"");
    }

    @Test
    void shouldSearchTotalRatesAcrossMailClasses() {
        String totalRatesJson = """
                {
                  "zone": "3",
                  "rates": [
                    {
                      "mailClass": "USPS_GROUND_ADVANTAGE",
                      "price": 6.50,
                      "zone": "3"
                    },
                    {
                      "mailClass": "PRIORITY_MAIL",
                      "price": 9.20,
                      "zone": "3"
                    },
                    {
                      "mailClass": "PRIORITY_MAIL_EXPRESS",
                      "price": 28.75,
                      "zone": "3"
                    }
                  ]
                }
                """;
        mockHttpClient.onRoute("/prices/v3/total-rates/search", req ->
                MockHttpClient.MockResponseSpec.of(200, totalRatesJson));

        TotalRatesRequest request = TotalRatesRequest.builder()
                .originZIPCode("30301")
                .destinationZIPCode("33101")
                .weight(1.2)
                .dimensions(8.0, 6.0, 3.0)
                .build();

        RateResponse response = client.prices().searchTotalRates(request);

        assertThat(response).isNotNull();
        assertThat(response.getRates()).hasSize(3);
        assertThat(response.getRates().get(0).getMailClass()).isEqualTo("USPS_GROUND_ADVANTAGE");
        assertThat(response.getRates().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("6.50"));
        assertThat(response.getRates().get(2).getMailClass()).isEqualTo("PRIORITY_MAIL_EXPRESS");
    }

    @Test
    void shouldValidateRateRequestInputs() {
        assertThatThrownBy(() -> BaseRatesRequest.builder()
                .originZIPCode("10001")
                .destinationZIPCode("90210")
                .weight(0)
                .build())
                .isInstanceOf(UspsValidationException.class)
                .hasMessageContaining("weight must be greater than zero");

        assertThatThrownBy(() -> BaseRatesRequest.builder()
                .originZIPCode("")
                .destinationZIPCode("90210")
                .weight(1.0)
                .build())
                .isInstanceOf(UspsValidationException.class)
                .hasMessageContaining("originZIPCode is required");
    }

    @Test
    void shouldCalculateRatesAsynchronously() throws Exception {
        mockHttpClient.onRoute("/prices/v3/base-rates/search", req ->
                MockHttpClient.MockResponseSpec.of(200, "{\"totalBasePrice\": 12.50}"));

        BaseRatesRequest request = BaseRatesRequest.builder()
                .originZIPCode("10001")
                .destinationZIPCode("90210")
                .weight(3.0)
                .build();

        CompletableFuture<RateResponse> future = client.prices().calculateBaseRatesAsync(request);
        RateResponse response = future.get();

        assertThat(response.getTotalBasePrice()).isEqualByComparingTo(new BigDecimal("12.50"));
    }
}

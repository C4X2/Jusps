package com.usps.sdk.service;

import com.usps.sdk.UspsClient;
import com.usps.sdk.config.UspsEnvironment;
import com.usps.sdk.exception.UspsValidationException;
import com.usps.sdk.http.MockHttpClient;
import com.usps.sdk.model.common.Address;
import com.usps.sdk.model.common.MailClass;
import com.usps.sdk.model.labels.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LabelsServiceTest {

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
    void shouldCreateLabelSuccessfully() {
        String dummyPdfContent = "%PDF-1.4 Mock Label Data";
        String base64Pdf = Base64.getEncoder().encodeToString(dummyPdfContent.getBytes(StandardCharsets.UTF_8));

        String labelResponseJson = """
                {
                  "labelId": "LBL_987654321",
                  "trackingNumber": "9400111899223344556677",
                  "labelImage": "%s",
                  "totalPrice": 8.75,
                  "routingCode": "440-10001"
                }
                """.formatted(base64Pdf);

        mockHttpClient.onRoute("/labels/v3/label", req ->
                MockHttpClient.MockResponseSpec.of(200, labelResponseJson));

        Address toAddress = Address.builder()
                .firstName("Jane")
                .lastName("Doe")
                .streetAddress("123 Maple St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .build();

        Address fromAddress = Address.builder()
                .firstName("Bob")
                .lastName("Smith")
                .streetAddress("456 Oak Ave")
                .city("Los Angeles")
                .state("CA")
                .zipCode("90001")
                .build();

        PackageDescription packageDesc = PackageDescription.builder()
                .mailClass(MailClass.USPS_GROUND_ADVANTAGE)
                .weight(1.5)
                .dimensions(8.0, 6.0, 2.0)
                .build();

        CreateLabelRequest request = CreateLabelRequest.builder()
                .imageInfo(ImageInfo.builder().imageType(ImageType.PDF).labelType(LabelType.LABEL_4X6).build())
                .toAddress(toAddress)
                .fromAddress(fromAddress)
                .packageDescription(packageDesc)
                .paymentAuthorizationToken("PAY_TOKEN_ABC123")
                .build();

        LabelResponse response = client.labels().createLabel(request);

        assertThat(response).isNotNull();
        assertThat(response.getLabelId()).isEqualTo("LBL_987654321");
        assertThat(response.getTrackingNumber()).isEqualTo("9400111899223344556677");
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("8.75"));

        byte[] decodedBytes = response.getDecodedLabelImage();
        assertThat(decodedBytes).isNotNull();
        assertThat(new String(decodedBytes, StandardCharsets.UTF_8)).isEqualTo(dummyPdfContent);

        // Verify request payload and payment token header
        var lastRequest = mockHttpClient.getLastRequest();
        assertThat(lastRequest.headers().firstValue("X-Payment-Authorization-Token")).contains("PAY_TOKEN_ABC123");
        var recordedBody = mockHttpClient.getLastRequestBody();
        assertThat(recordedBody).contains("\"toAddress\":");
        assertThat(recordedBody).contains("\"123 Maple St\"");
        assertThat(recordedBody).contains("\"fromAddress\":");
        assertThat(recordedBody).contains("\"imageType\":\"PDF\"");
    }

    @Test
    void shouldRetrieveLabelById() {
        mockHttpClient.onRoute("/labels/v3/label/LBL_123", req ->
                MockHttpClient.MockResponseSpec.of(200, "{\"labelId\":\"LBL_123\",\"trackingNumber\":\"940000000000\"}"));

        LabelResponse response = client.labels().getLabel("LBL_123");

        assertThat(response).isNotNull();
        assertThat(response.getLabelId()).isEqualTo("LBL_123");
        assertThat(response.getTrackingNumber()).isEqualTo("940000000000");
    }

    @Test
    void shouldCancelLabel() {
        mockHttpClient.onRoute("/labels/v3/label/LBL_123", req ->
                MockHttpClient.MockResponseSpec.of(200, "{}"));

        client.labels().cancelLabel("LBL_123");

        var lastRequest = mockHttpClient.getLastRequest();
        assertThat(lastRequest.method()).isEqualTo("DELETE");
        assertThat(lastRequest.uri().getPath()).isEqualTo("/labels/v3/label/LBL_123");
    }

    @Test
    void shouldValidateLabelRequestParameters() {
        assertThatThrownBy(() -> CreateLabelRequest.builder()
                .toAddress(Address.builder().streetAddress("123 St").build())
                .build())
                .isInstanceOf(UspsValidationException.class)
                .hasMessageContaining("fromAddress is required");

        assertThatThrownBy(() -> client.labels().getLabel(""))
                .isInstanceOf(UspsValidationException.class)
                .hasMessageContaining("labelId cannot be null or blank");
    }

    @Test
    void shouldSupportAsyncLabelCreation() throws Exception {
        mockHttpClient.onRoute("/labels/v3/label", req ->
                MockHttpClient.MockResponseSpec.of(200, "{\"labelId\":\"ASYNC_LBL\"}"));

        CreateLabelRequest request = CreateLabelRequest.builder()
                .toAddress(Address.builder().streetAddress("123 St").city("A").state("B").zipCode("12345").build())
                .fromAddress(Address.builder().streetAddress("456 St").city("C").state("D").zipCode("67890").build())
                .packageDescription(PackageDescription.builder().weight(1.0).build())
                .build();

        CompletableFuture<LabelResponse> future = client.labels().createLabelAsync(request);
        LabelResponse response = future.get();

        assertThat(response.getLabelId()).isEqualTo("ASYNC_LBL");
    }
}

package io.github.c4x2.jusps;

import io.github.c4x2.jusps.config.UspsConfig;
import io.github.c4x2.jusps.config.UspsEnvironment;
import io.github.c4x2.jusps.exception.UspsValidationException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UspsClientTest {

    @Test
    void shouldCreateClientWithDefaults() {
        UspsClient client = UspsClient.create("MOCK_CLIENT_ID", "MOCK_SECRET");

        assertThat(client).isNotNull();
        assertThat(client.getConfig().getClientId()).isEqualTo("MOCK_CLIENT_ID");
        assertThat(client.getConfig().getClientSecret()).isEqualTo("MOCK_SECRET");
        assertThat(client.getConfig().getEnvironment()).isEqualTo(UspsEnvironment.PRODUCTION);
        assertThat(client.getConfig().getEnvironment().getBaseUrl()).isEqualTo("https://apis.usps.com");
        assertThat(client.getConfig().getMaxRetries()).isEqualTo(3);
        assertThat(client.getConfig().getConnectTimeout()).isEqualTo(Duration.ofSeconds(15));
        assertThat(client.getConfig().getReadTimeout()).isEqualTo(Duration.ofSeconds(30));

        // Subservices
        assertThat(client.addresses()).isNotNull();
        assertThat(client.tracking()).isNotNull();
        assertThat(client.prices()).isNotNull();
        assertThat(client.labels()).isNotNull();
        assertThat(client.serviceStandards()).isNotNull();
        assertThat(client.locations()).isNotNull();
        assertThat(client.carrierPickup()).isNotNull();
        assertThat(client.tokenManager()).isNotNull();
        assertThat(client.getTransport()).isNotNull();
    }

    @Test
    void shouldCreateClientWithCustomConfig() {
        UspsClient client = UspsClient.builder()
                .clientId("TEST_KEY")
                .clientSecret("TEST_SECRET")
                .environment(UspsEnvironment.TESTING)
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(20))
                .maxRetries(5)
                .paymentAuthorizationToken("TEST_PAY_TOKEN")
                .crid("1234567")
                .mid("9876543")
                .buildClient();

        assertThat(client.getConfig().getEnvironment()).isEqualTo(UspsEnvironment.TESTING);
        assertThat(client.getConfig().getEnvironment().getBaseUrl()).isEqualTo("https://apis-tem.usps.com");
        assertThat(client.getConfig().getConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(client.getConfig().getReadTimeout()).isEqualTo(Duration.ofSeconds(20));
        assertThat(client.getConfig().getMaxRetries()).isEqualTo(5);
        assertThat(client.getConfig().getPaymentAuthorizationToken()).isEqualTo("TEST_PAY_TOKEN");
        assertThat(client.getConfig().getCrid()).isEqualTo("1234567");
        assertThat(client.getConfig().getMid()).isEqualTo("9876543");
    }

    @Test
    void shouldValidateClientIdAndSecret() {
        assertThatThrownBy(() -> UspsConfig.builder().clientSecret("secret").build())
                .isInstanceOf(UspsValidationException.class)
                .hasMessageContaining("clientId must not be null or blank");

        assertThatThrownBy(() -> UspsConfig.builder().clientId("key").build())
                .isInstanceOf(UspsValidationException.class)
                .hasMessageContaining("clientSecret must not be null or blank");
    }
}

package io.github.c4x2.jusps;

import io.github.c4x2.jusps.auth.AuthenticationService;
import io.github.c4x2.jusps.auth.TokenManager;
import io.github.c4x2.jusps.config.UspsConfig;
import io.github.c4x2.jusps.config.UspsEnvironment;
import io.github.c4x2.jusps.http.HttpTransport;
import io.github.c4x2.jusps.service.*;

import java.net.http.HttpClient;
import java.util.Objects;

/**
 * Main entry point for interacting with the USPS Developer Portal REST APIs (v3).
 *
 * <p>Example usage:
 * <pre>{@code
 * UspsClient client = UspsClient.builder()
 *     .clientId("YOUR_CONSUMER_KEY")
 *     .clientSecret("YOUR_CONSUMER_SECRET")
 *     .environment(UspsEnvironment.TESTING)
 *     .build();
 *
 * // Standardize an address
 * AddressResponse response = client.addresses().standardizeAddress(AddressRequest.builder()
 *     .streetAddress("1600 Pennsylvania Ave NW")
 *     .city("Washington")
 *     .state("DC")
 *     .zipCode("20500")
 *     .build());
 *
 * // Track a package
 * TrackInfo tracking = client.tracking().getTracking("9400100000000000000000");
 * }</pre>
 */
public class UspsClient implements AutoCloseable {

    @lombok.Getter
    private final UspsConfig config;
    private final HttpClient httpClient;
    private final TokenManager tokenManager;
    @lombok.Getter
    private final HttpTransport transport;

    private final AddressesService addressesService;
    private final TrackingService trackingService;
    private final PricesService pricesService;
    private final LabelsService labelsService;
    private final ServiceStandardsService serviceStandardsService;
    private final LocationsService locationsService;
    private final CarrierPickupService carrierPickupService;

    public UspsClient(UspsConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");

        if (config.getHttpClient() != null) {
            this.httpClient = config.getHttpClient();
        } else {
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(config.getConnectTimeout())
                    .version(HttpClient.Version.HTTP_2)
                    .build();
        }

        AuthenticationService authService = new AuthenticationService(config, this.httpClient);
        this.tokenManager = new TokenManager(authService);
        this.transport = new HttpTransport(config, this.httpClient, this.tokenManager);

        this.addressesService = new AddressesService(this.transport);
        this.trackingService = new TrackingService(this.transport);
        this.pricesService = new PricesService(this.transport);
        this.labelsService = new LabelsService(this.transport);
        this.serviceStandardsService = new ServiceStandardsService(this.transport);
        this.locationsService = new LocationsService(this.transport);
        this.carrierPickupService = new CarrierPickupService(this.transport);
    }

    /**
     * Creates a new client builder.
     */
    public static UspsConfig.Builder builder() {
        return UspsConfig.builder();
    }

    /**
     * Creates a standard client configured for production.
     */
    public static UspsClient create(String clientId, String clientSecret) {
        return builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .environment(UspsEnvironment.PRODUCTION)
                .buildClient();
    }

    /**
     * Creates a standard client configured for a specific environment.
     */
    public static UspsClient create(String clientId, String clientSecret, UspsEnvironment environment) {
        return builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .environment(environment)
                .buildClient();
    }

    public AddressesService addresses() { return addressesService; }
    public TrackingService tracking() { return trackingService; }
    public PricesService prices() { return pricesService; }
    public LabelsService labels() { return labelsService; }
    public ServiceStandardsService serviceStandards() { return serviceStandardsService; }
    public LocationsService locations() { return locationsService; }
    public CarrierPickupService carrierPickup() { return carrierPickupService; }
    public TokenManager tokenManager() { return tokenManager; }

    @Override
    public void close() {
        // HttpClient in modern Java is managed or will clean up daemon threads automatically.
    }
}

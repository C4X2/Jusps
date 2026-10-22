# USPS Java SDK (`jusps`)

A modern, high-performance, developer-friendly Java SDK for the official **USPS Developer Portal REST APIs (v3)**.

Built on Java 21 LTS with Project Lombok, Jackson, native `java.net.http.HttpClient`, and SLF4J, featuring automatic OAuth 2.0 token management, thread-safe caching, exponential backoff retries, and comprehensive support for both synchronous and asynchronous operations (`CompletableFuture`).

---

## Features

- **Official USPS v3 Platform Support**: Full support for USPS RESTful endpoints secured with OAuth 2.0 Bearer tokens.
- **Lombok Integration**: Minimal boilerplate with `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor`.
- **Automatic OAuth 2.0 Token Lifecycle**:
  - Thread-safe token acquisition and caching using `client_credentials`.
  - Automatic preemptive token refresh before expiration with safety buffer.
  - Transparent retry on 401 Unauthorized invalidations.
- **Resilience & Fault Tolerance**:
  - Configurable exponential backoff retries with jitter for HTTP 429 (Rate Limits) and HTTP 5xx (Server Errors).
  - Respects HTTP `Retry-After` header directives.
- **Dual Execution Models**: Every API operation supports both synchronous and asynchronous (`CompletableFuture<T>`) execution.
- **Comprehensive API Coverage**:
  - **Addresses 3.0**: Standardization, DPV (Delivery Point Validation) verification, City/State lookup, and ZIP Code lookup.
  - **Tracking 3.0**: Single package tracking, status categories, delivery timestamps, and scan events history (`expand=DETAIL` or `expand=SUMMARY`).
  - **Prices 3.0**: Base postage rate calculation and cross-service total rate searches.
  - **Labels 3.0**: Certified domestic shipping labels generation (PDF, ZPL, PNG), Base64 automatic decoding, EPS/Permit payment tokens, label inquiry, and cancellation.
  - **Service Standards 3.0**: Benchmark delivery day transit estimates between origin and destination ZIP codes.
  - **Locations 3.0**: Retail Post Office finder with search radius and drop-off facility discovery for destination entry discounts.
  - **Carrier Pickup 3.0**: Pickup eligibility checks, next-day carrier pickup scheduling, inquiry, and cancellation.

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.c4x2:jusps:1.0.0")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.c4x2:jusps:1.0.0'
    compileOnly 'org.projectlombok:lombok:1.18.34'
    annotationProcessor 'org.projectlombok:lombok:1.18.34'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.c4x2</groupId>
    <artifactId>jusps</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.34</version>
    <scope>provided</scope>
</dependency>
```

---

## Quickstart

### 1. Initialize the Client

```java
import io.github.c4x2.jusps.UspsClient;
import io.github.c4x2.jusps.config.UspsEnvironment;
import java.time.Duration;

UspsClient client = UspsClient.builder()
    .clientId("YOUR_USPS_CONSUMER_KEY")
    .clientSecret("YOUR_USPS_CONSUMER_SECRET")
    .environment(UspsEnvironment.TESTING) // UspsEnvironment.PRODUCTION for live traffic
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .maxRetries(3)
    .buildClient();
```

---

## API Usage Examples

### 1. Address Standardization & Validation (Addresses 3.0)

```java
import io.github.c4x2.jusps.model.address.AddressRequest;
import io.github.c4x2.jusps.model.address.AddressResponse;

AddressRequest request = AddressRequest.builder()
    .streetAddress("1600 Pennsylvania Ave NW")
    .city("Washington")
    .state("DC")
    .zipCode("20500")
    .build();

AddressResponse response = client.addresses().standardizeAddress(request);

if (response.isValid()) {
    System.out.println("Standardized Street: " + response.getAddress().getStreetAddress());
    System.out.println("ZIP+4: " + response.getAddress().getFullZip());
    System.out.println("Carrier Route: " + response.getAdditionalInfo().getCarrierRoute());
    System.out.println("Is Business: " + response.getAdditionalInfo().isBusinessAddress());
}
```

#### City / State & ZIP Code Lookups

```java
// City/State lookup by 5-digit ZIP
CityStateResponse cityState = client.addresses().lookupCityState("90210");
System.out.println(cityState.getCity() + ", " + cityState.getState()); // BEVERLY HILLS, CA

// ZIP code lookup by address
ZipCodeLookupResponse zipResponse = client.addresses().lookupZipCode(
    ZipCodeLookupRequest.builder()
        .streetAddress("475 L'Enfant Plaza SW")
        .city("Washington")
        .state("DC")
        .build()
);
System.out.println("ZIP Code: " + zipResponse.getAddress().getFullZip());
```

---

### 2. Package Tracking (Tracking 3.0)

```java
import io.github.c4x2.jusps.model.tracking.TrackInfo;
import io.github.c4x2.jusps.model.tracking.TrackingEvent;

TrackInfo tracking = client.tracking().getTracking("9400100000000000000000");

System.out.println("Status: " + tracking.getStatusCategory());
System.out.println("Summary: " + tracking.getTrackSummary());
System.out.println("Delivered: " + tracking.isDelivered());
System.out.println("Expected Delivery: " + tracking.getExpectedDeliveryDate());

// Scan History
for (TrackingEvent event : tracking.getTrackDetail()) {
    System.out.println(event.getEventTimestamp() + " - " + event.getEventDescription() + " (" + event.getEventCity() + ", " + event.getEventState() + ")");
}
```

---

### 3. Postage Rates (Prices 3.0)

#### Calculate Base Rates for a Specific Mail Class

```java
import io.github.c4x2.jusps.model.common.MailClass;
import io.github.c4x2.jusps.model.common.PriceType;
import io.github.c4x2.jusps.model.prices.BaseRatesRequest;
import io.github.c4x2.jusps.model.prices.RateResponse;

BaseRatesRequest rateRequest = BaseRatesRequest.builder()
    .originZIPCode("90210")
    .destinationZIPCode("10001")
    .weight(2.5) // lbs
    .dimensions(10.0, 5.0, 4.0) // length, width, height (inches)
    .mailClass(MailClass.PRIORITY_MAIL)
    .priceType(PriceType.RETAIL)
    .build();

RateResponse rate = client.prices().calculateBaseRates(rateRequest);
System.out.println("Postage: $" + rate.getTotalBasePrice() + " (Zone " + rate.getZone() + ")");
```

#### Total Rates Search Across All Eligible Services

```java
import io.github.c4x2.jusps.model.prices.TotalRatesRequest;
import io.github.c4x2.jusps.model.prices.RateOption;

TotalRatesRequest totalRequest = TotalRatesRequest.builder()
    .originZIPCode("30301")
    .destinationZIPCode("33101")
    .weight(1.5)
    .dimensions(8.0, 6.0, 3.0)
    .build();

RateResponse allRates = client.prices().searchTotalRates(totalRequest);
for (RateOption opt : allRates.getRates()) {
    System.out.println(opt.getMailClass() + ": $" + opt.getPrice());
}
```

---

### 4. Shipping Labels (Labels 3.0)

```java
import io.github.c4x2.jusps.model.common.Address;
import io.github.c4x2.jusps.model.common.MailClass;
import io.github.c4x2.jusps.model.labels.*;
import java.nio.file.Files;
import java.nio.file.Paths;

CreateLabelRequest labelRequest = CreateLabelRequest.builder()
    .imageInfo(ImageInfo.builder()
        .imageType(ImageType.PDF)
        .labelType(LabelType.LABEL_4X6)
        .build())
    .toAddress(Address.builder()
        .firstName("Jane")
        .lastName("Doe")
        .streetAddress("123 Maple St")
        .city("New York")
        .state("NY")
        .zipCode("10001")
        .build())
    .fromAddress(Address.builder()
        .firstName("John")
        .lastName("Smith")
        .streetAddress("456 Oak Ave")
        .city("Los Angeles")
        .state("CA")
        .zipCode("90001")
        .build())
    .packageDescription(PackageDescription.builder()
        .mailClass(MailClass.PRIORITY_MAIL)
        .weight(2.0)
        .dimensions(10.0, 6.0, 4.0)
        .build())
    .paymentAuthorizationToken("OPTIONAL_PERMIT_OR_EPS_TOKEN")
    .build();

LabelResponse label = client.labels().createLabel(labelRequest);

System.out.println("Tracking Barcode: " + label.getTrackingNumber());
System.out.println("Label ID: " + label.getLabelId());

// Save PDF label directly to disk
byte[] pdfBytes = label.getDecodedLabelImage();
Files.write(Paths.get("shipping-label.pdf"), pdfBytes);

// Cancel / void label if needed
client.labels().cancelLabel(label.getLabelId());
```

---

### 5. Service Delivery Standards (Service Standards 3.0)

```java
import io.github.c4x2.jusps.model.standards.ServiceStandardRequest;
import io.github.c4x2.jusps.model.standards.ServiceStandardResponse;
import io.github.c4x2.jusps.model.standards.ServiceStandardEstimate;

ServiceStandardRequest request = ServiceStandardRequest.builder()
    .originZIPCode("10018")
    .destinationZIPCode("95823")
    .build();

ServiceStandardResponse response = client.serviceStandards().getEstimates(request);
for (ServiceStandardEstimate estimate : response.getEstimates()) {
    System.out.println(estimate.getMailClass() + " - " + estimate.getDeliveryDays() + " Days (Est. " + estimate.getEstimatedDeliveryDate() + ")");
}
```

---

### 6. Locations (Locations 3.0)

```java
import io.github.c4x2.jusps.model.locations.PostOfficeLocationsRequest;
import io.github.c4x2.jusps.model.locations.LocationResponse;
import io.github.c4x2.jusps.model.locations.LocationFacility;

// Find retail Post Offices within 5 miles
LocationResponse postOffices = client.locations().getPostOffices(
    PostOfficeLocationsRequest.builder()
        .zipCode("10001")
        .radius(5)
        .build()
);

for (LocationFacility po : postOffices.getLocations()) {
    System.out.println(po.getName() + " - " + po.getAddress().getStreetAddress());
}
```

---

### 7. Carrier Package Pickup (Carrier Pickup 3.0)

```java
import io.github.c4x2.jusps.model.pickup.*;
import io.github.c4x2.jusps.model.common.Address;

Address pickupAddr = Address.builder()
    .streetAddress("123 Main St")
    .city("Miami")
    .state("FL")
    .zipCode("33101")
    .build();

// Check availability
PickupAvailabilityResponse avail = client.carrierPickup().checkAvailability(new PickupAvailabilityRequest(pickupAddr));

if (avail.isAvailable()) {
    // Schedule pickup
    SchedulePickupRequest schedule = SchedulePickupRequest.builder()
        .pickupAddress(pickupAddr)
        .pickupDate("2026-10-24")
        .packageLocation("Front Porch")
        .packageCount(PickupPackageCount.builder()
            .priorityMailCount(2)
            .groundAdvantageCount(1)
            .build())
        .specialInstructions("Ring doorbell upon arrival")
        .build();

    PickupResponse confirmation = client.carrierPickup().schedulePickup(schedule);
    System.out.println("Confirmation #: " + confirmation.getConfirmationNumber());

    // Inquire pickup status
    PickupResponse status = client.carrierPickup().inquirePickup(confirmation.getConfirmationNumber());

    // Cancel pickup
    client.carrierPickup().cancelPickup(confirmation.getConfirmationNumber());
}
```

---

### 8. Asynchronous Operations

All service operations offer `*Async` counterparts returning `CompletableFuture<T>`:

```java
client.addresses().standardizeAddressAsync(request)
    .thenAccept(addr -> System.out.println("Normalized: " + addr.getAddress().getStreetAddress()))
    .exceptionally(ex -> {
        System.err.println("Failed: " + ex.getMessage());
        return null;
    });

client.tracking().getTrackingAsync("9400100000000000000000")
    .thenAccept(track -> System.out.println("Status: " + track.getStatusCategory()));
```

---

## Error Handling

The SDK maps all HTTP and business errors into a clean, strongly-typed exception hierarchy:

```
UspsException (RuntimeException)
 ├── UspsValidationException (Client-side validation errors)
 ├── UspsSerializationException (JSON formatting/parsing failures)
 └── UspsApiException (Server HTTP errors)
      ├── UspsAuthenticationException (HTTP 401 Unauthorized)
      ├── UspsAuthorizationException (HTTP 403 Forbidden)
      ├── UspsNotFoundException (HTTP 404 Not Found)
      ├── UspsRateLimitException (HTTP 429 Too Many Requests - with retryAfter duration)
      └── UspsServerException (HTTP 500, 502, 503, 504)
```

```java
import io.github.c4x2.jusps.exception.*;

try {
    client.tracking().getTracking("INVALID_NUMBER");
} catch (UspsNotFoundException e) {
    System.err.println("Package not found: " + e.getMessage());
} catch (UspsRateLimitException e) {
    System.err.println("Rate limited. Retry after: " + e.getRetryAfter().getSeconds() + "s");
} catch (UspsAuthenticationException e) {
    System.err.println("Bad credentials: " + e.getStatusCode());
} catch (UspsApiException e) {
    System.err.println("USPS error " + e.getStatusCode() + ": " + e.getRawResponseBody());
}
```

---

## Building & Testing

```bash
# Run test suite
./gradlew test

# Build library jar, javadoc jar, and sources jar
./gradlew build
```

---

## License

MIT License. See [LICENSE](LICENSE) for details.

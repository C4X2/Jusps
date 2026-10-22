package io.github.c4x2.jusps.model.pickup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.c4x2.jusps.exception.UspsValidationException;
import io.github.c4x2.jusps.model.common.Address;
import lombok.*;

import java.time.LocalDate;

/**
 * Request payload to schedule a carrier pickup (POST /pickup/v3/carrier-pickup).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchedulePickupRequest {

    @JsonProperty("pickupAddress")
    private Address pickupAddress;

    @JsonProperty("pickupDate")
    private String pickupDate;

    @JsonProperty("packageCount")
    private PickupPackageCount packageCount;

    @JsonProperty("packageLocation")
    private String packageLocation;

    @JsonProperty("specialInstructions")
    private String specialInstructions;

    public static class SchedulePickupRequestBuilder {
        private String pickupDate = LocalDate.now().plusDays(1).toString();
        private String packageLocation = "Front Door";

        public SchedulePickupRequestBuilder pickupDate(LocalDate date) {
            this.pickupDate = date != null ? date.toString() : null;
            return this;
        }

        public SchedulePickupRequestBuilder pickupDate(String date) {
            this.pickupDate = date;
            return this;
        }

        public SchedulePickupRequest build() {
            if (pickupAddress == null) {
                throw new UspsValidationException("pickupAddress is required to schedule a pickup");
            }
            if (pickupDate == null || pickupDate.trim().isEmpty()) {
                throw new UspsValidationException("pickupDate is required to schedule a pickup");
            }
            if (packageCount == null || packageCount.getTotalCount() == 0) {
                throw new UspsValidationException("At least one package must be specified in packageCount");
            }
            return new SchedulePickupRequest(pickupAddress, pickupDate, packageCount, packageLocation, specialInstructions);
        }
    }
}

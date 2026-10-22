package io.github.c4x2.jusps.model.pickup;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Result returned upon scheduling, inquiring, or canceling a carrier pickup.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PickupResponse {

    @JsonProperty("confirmationNumber")
    @JsonAlias({"confirmationId", "pickupConfirmationNumber"})
    private String confirmationNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("pickupDate")
    private String pickupDate;

    @JsonProperty("packageCount")
    private Integer packageCount;

    @JsonProperty("message")
    private String message;

    @Override
    public String toString() {
        return "PickupResponse{" +
                "confirmationNumber='" + confirmationNumber + '\'' +
                ", status='" + status + '\'' +
                ", pickupDate='" + pickupDate + '\'' +
                '}';
    }
}

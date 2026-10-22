package io.github.c4x2.jusps.model.standards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * An individual transit time estimate for a specific mail class.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceStandardEstimate {

    @JsonProperty("mailClass")
    private String mailClass;

    @JsonProperty("serviceTypeDescription")
    private String serviceTypeDescription;

    @JsonProperty("deliveryDays")
    private Integer deliveryDays;

    @JsonProperty("effectiveAcceptanceDate")
    private String effectiveAcceptanceDate;

    @JsonProperty("estimatedDeliveryDate")
    private String estimatedDeliveryDate;

    @Override
    public String toString() {
        return "ServiceStandardEstimate{" +
                "mailClass='" + mailClass + '\'' +
                ", deliveryDays=" + deliveryDays +
                ", estimatedDeliveryDate='" + estimatedDeliveryDate + '\'' +
                '}';
    }
}

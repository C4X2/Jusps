package io.github.c4x2.jusps.model.pickup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Counts of packages grouped by mail class for a scheduled pickup.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PickupPackageCount {

    @JsonProperty("priorityMailExpressCount")
    private int priorityMailExpressCount;

    @JsonProperty("priorityMailCount")
    private int priorityMailCount;

    @JsonProperty("groundAdvantageCount")
    private int groundAdvantageCount;

    @JsonProperty("internationalCount")
    private int internationalCount;

    @JsonProperty("returnsCount")
    private int returnsCount;

    public int getTotalCount() {
        return priorityMailExpressCount + priorityMailCount + groundAdvantageCount + internationalCount + returnsCount;
    }
}

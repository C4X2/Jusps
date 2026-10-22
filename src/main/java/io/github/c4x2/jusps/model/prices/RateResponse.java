package io.github.c4x2.jusps.model.prices;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the pricing response returned by USPS Prices API endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RateResponse {

    @JsonProperty("totalBasePrice")
    @JsonAlias({"price", "totalPrice", "basePrice"})
    private BigDecimal totalBasePrice;

    @JsonProperty("rates")
    @JsonAlias({"rateOptions", "priceList", "rateList"})
    private List<RateOption> rates;

    @JsonProperty("zone")
    private String zone;

    public List<RateOption> getRates() {
        return rates != null ? rates : Collections.emptyList();
    }

    /**
     * Gets the first or cheapest rate option if rates list is present.
     */
    public RateOption getPrimaryRate() {
        if (rates != null && !rates.isEmpty()) {
            return rates.get(0);
        }
        return null;
    }

    @Override
    public String toString() {
        return "RateResponse{" +
                "totalBasePrice=" + totalBasePrice +
                ", zone='" + zone + '\'' +
                ", ratesCount=" + (rates != null ? rates.size() : 0) +
                '}';
    }
}

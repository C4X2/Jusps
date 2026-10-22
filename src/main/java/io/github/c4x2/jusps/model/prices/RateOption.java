package io.github.c4x2.jusps.model.prices;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Details of a specific USPS rate quote option.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RateOption {

    @JsonProperty("mailClass")
    private String mailClass;

    @JsonProperty("mailClassDescription")
    private String mailClassDescription;

    @JsonProperty("rateIndicator")
    private String rateIndicator;

    @JsonProperty("priceType")
    private String priceType;

    @JsonProperty("price")
    @JsonAlias({"totalBasePrice", "totalPrice", "basePrice"})
    private BigDecimal price;

    @JsonProperty("zone")
    private String zone;

    @JsonProperty("extraServices")
    private List<ExtraService> extraServices;

    public List<ExtraService> getExtraServices() {
        return extraServices != null ? extraServices : Collections.emptyList();
    }

    @Override
    public String toString() {
        return "RateOption{" +
                "mailClass='" + mailClass + '\'' +
                ", price=" + price +
                ", zone='" + zone + '\'' +
                '}';
    }
}

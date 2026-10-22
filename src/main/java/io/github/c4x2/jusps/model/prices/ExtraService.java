package io.github.c4x2.jusps.model.prices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents an optional ancillary service (e.g., Signature Confirmation, Certified Mail, Insurance).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtraService {

    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("selected")
    private Boolean selected;

    @Override
    public String toString() {
        return "ExtraService{" + name + " ($" + price + ")}";
    }
}

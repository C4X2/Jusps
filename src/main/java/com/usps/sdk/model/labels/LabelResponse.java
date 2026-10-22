package com.usps.sdk.model.labels;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Base64;

/**
 * Result returned upon successful creation or retrieval of a USPS shipping label.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelResponse {

    @JsonProperty("labelId")
    private String labelId;

    @JsonProperty("trackingNumber")
    private String trackingNumber;

    @JsonProperty("labelImage")
    @JsonAlias({"labelData", "image"})
    private String labelImage;

    @JsonProperty("totalPrice")
    @JsonAlias({"price", "totalBasePrice"})
    private BigDecimal totalPrice;

    @JsonProperty("routingCode")
    private String routingCode;

    /**
     * Decodes the Base64 label image string into binary data (PDF, ZPL, PNG, etc.).
     *
     * @return raw binary image bytes, or null if no label image is present
     */
    public byte[] getDecodedLabelImage() {
        if (labelImage == null || labelImage.trim().isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(labelImage.trim());
    }

    @Override
    public String toString() {
        return "LabelResponse{" +
                "labelId='" + labelId + '\'' +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", totalPrice=" + totalPrice +
                '}';
    }
}

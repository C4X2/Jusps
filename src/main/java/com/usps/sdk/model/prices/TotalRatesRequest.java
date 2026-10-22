package com.usps.sdk.model.prices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.usps.sdk.exception.UspsValidationException;
import com.usps.sdk.model.common.PriceType;
import com.usps.sdk.model.common.ProcessingCategory;
import lombok.*;

import java.time.LocalDate;

/**
 * Request payload for querying total USPS postage rates across services (POST /prices/v3/total-rates/search).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TotalRatesRequest {

    @JsonProperty("originZIPCode")
    private String originZIPCode;

    @JsonProperty("destinationZIPCode")
    private String destinationZIPCode;

    @JsonProperty("weight")
    private double weight;

    @JsonProperty("length")
    private double length;

    @JsonProperty("width")
    private double width;

    @JsonProperty("height")
    private double height;

    @JsonProperty("processingCategory")
    private String processingCategory;

    @JsonProperty("priceType")
    private String priceType;

    @JsonProperty("mailingDate")
    private String mailingDate;

    public static class TotalRatesRequestBuilder {
        private String processingCategory = ProcessingCategory.MACHINABLE.name();
        private String priceType = PriceType.RETAIL.name();
        private String mailingDate = LocalDate.now().toString();

        public TotalRatesRequestBuilder dimensions(double length, double width, double height) {
            this.length = length;
            this.width = width;
            this.height = height;
            return this;
        }

        public TotalRatesRequestBuilder processingCategory(ProcessingCategory category) {
            this.processingCategory = category.name();
            return this;
        }

        public TotalRatesRequestBuilder processingCategory(String category) {
            this.processingCategory = category;
            return this;
        }

        public TotalRatesRequestBuilder priceType(PriceType priceType) {
            this.priceType = priceType.name();
            return this;
        }

        public TotalRatesRequestBuilder priceType(String priceType) {
            this.priceType = priceType;
            return this;
        }

        public TotalRatesRequestBuilder mailingDate(LocalDate date) {
            this.mailingDate = date != null ? date.toString() : null;
            return this;
        }

        public TotalRatesRequestBuilder mailingDate(String date) {
            this.mailingDate = date;
            return this;
        }

        public TotalRatesRequest build() {
            if (originZIPCode == null || originZIPCode.trim().isEmpty()) {
                throw new UspsValidationException("originZIPCode is required");
            }
            if (destinationZIPCode == null || destinationZIPCode.trim().isEmpty()) {
                throw new UspsValidationException("destinationZIPCode is required");
            }
            if (weight <= 0) {
                throw new UspsValidationException("weight must be greater than zero");
            }
            return new TotalRatesRequest(originZIPCode, destinationZIPCode, weight, length, width, height,
                    processingCategory, priceType, mailingDate);
        }
    }
}

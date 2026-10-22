package io.github.c4x2.jusps.model.prices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.c4x2.jusps.exception.UspsValidationException;
import io.github.c4x2.jusps.model.common.MailClass;
import io.github.c4x2.jusps.model.common.PriceType;
import io.github.c4x2.jusps.model.common.ProcessingCategory;
import io.github.c4x2.jusps.model.common.RateIndicator;
import lombok.*;

import java.time.LocalDate;

/**
 * Request payload for calculating USPS base postage rates (POST /prices/v3/base-rates/search).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseRatesRequest {

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

    @JsonProperty("mailClass")
    private String mailClass;

    @JsonProperty("processingCategory")
    private String processingCategory;

    @JsonProperty("rateIndicator")
    private String rateIndicator;

    @JsonProperty("destinationEntryFacilityType")
    private String destinationEntryFacilityType;

    @JsonProperty("priceType")
    private String priceType;

    @JsonProperty("mailingDate")
    private String mailingDate;

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("accountNumber")
    private String accountNumber;

    public static class BaseRatesRequestBuilder {
        private String mailClass = MailClass.PRIORITY_MAIL.name();
        private String processingCategory = ProcessingCategory.MACHINABLE.name();
        private String rateIndicator = RateIndicator.SINGLE_PIECE.name();
        private String destinationEntryFacilityType = "NONE";
        private String priceType = PriceType.RETAIL.name();
        private String mailingDate = LocalDate.now().toString();

        public BaseRatesRequestBuilder dimensions(double length, double width, double height) {
            this.length = length;
            this.width = width;
            this.height = height;
            return this;
        }

        public BaseRatesRequestBuilder mailClass(MailClass mailClass) {
            this.mailClass = mailClass.name();
            return this;
        }

        public BaseRatesRequestBuilder mailClass(String mailClass) {
            this.mailClass = mailClass;
            return this;
        }

        public BaseRatesRequestBuilder processingCategory(ProcessingCategory category) {
            this.processingCategory = category.name();
            return this;
        }

        public BaseRatesRequestBuilder processingCategory(String category) {
            this.processingCategory = category;
            return this;
        }

        public BaseRatesRequestBuilder rateIndicator(RateIndicator indicator) {
            this.rateIndicator = indicator.name();
            return this;
        }

        public BaseRatesRequestBuilder rateIndicator(String indicator) {
            this.rateIndicator = indicator;
            return this;
        }

        public BaseRatesRequestBuilder priceType(PriceType priceType) {
            this.priceType = priceType.name();
            return this;
        }

        public BaseRatesRequestBuilder priceType(String priceType) {
            this.priceType = priceType;
            return this;
        }

        public BaseRatesRequestBuilder mailingDate(LocalDate date) {
            this.mailingDate = date != null ? date.toString() : null;
            return this;
        }

        public BaseRatesRequestBuilder mailingDate(String date) {
            this.mailingDate = date;
            return this;
        }

        public BaseRatesRequest build() {
            if (originZIPCode == null || originZIPCode.trim().isEmpty()) {
                throw new UspsValidationException("originZIPCode is required");
            }
            if (destinationZIPCode == null || destinationZIPCode.trim().isEmpty()) {
                throw new UspsValidationException("destinationZIPCode is required");
            }
            if (weight <= 0) {
                throw new UspsValidationException("weight must be greater than zero");
            }
            return new BaseRatesRequest(originZIPCode, destinationZIPCode, weight, length, width, height,
                    mailClass, processingCategory, rateIndicator, destinationEntryFacilityType,
                    priceType, mailingDate, accountType, accountNumber);
        }
    }
}

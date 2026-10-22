package io.github.c4x2.jusps.model.labels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.c4x2.jusps.model.common.MailClass;
import io.github.c4x2.jusps.model.common.ProcessingCategory;
import io.github.c4x2.jusps.model.common.RateIndicator;
import lombok.*;

import java.time.LocalDate;

/**
 * Physical dimensions, weight, and postal attributes for label generation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageDescription {

    @JsonProperty("mailClass")
    private String mailClass;

    @JsonProperty("rateIndicator")
    private String rateIndicator;

    @JsonProperty("weight")
    private double weight;

    @JsonProperty("weightUOM")
    private String weightUOM;

    @JsonProperty("dimensionsUOM")
    private String dimensionsUOM;

    @JsonProperty("length")
    private double length;

    @JsonProperty("width")
    private double width;

    @JsonProperty("height")
    private double height;

    @JsonProperty("processingCategory")
    private String processingCategory;

    @JsonProperty("mailingDate")
    private String mailingDate;

    @JsonProperty("destinationEntryFacilityType")
    private String destinationEntryFacilityType;

    public static class PackageDescriptionBuilder {
        private String mailClass = MailClass.PRIORITY_MAIL.name();
        private String rateIndicator = RateIndicator.SINGLE_PIECE.name();
        private String weightUOM = "lb";
        private String dimensionsUOM = "in";
        private String processingCategory = ProcessingCategory.MACHINABLE.name();
        private String mailingDate = LocalDate.now().toString();
        private String destinationEntryFacilityType = "NONE";

        public PackageDescriptionBuilder dimensions(double length, double width, double height) {
            this.length = length;
            this.width = width;
            this.height = height;
            return this;
        }

        public PackageDescriptionBuilder mailClass(MailClass mailClass) {
            this.mailClass = mailClass.name();
            return this;
        }

        public PackageDescriptionBuilder mailClass(String mailClass) {
            this.mailClass = mailClass;
            return this;
        }

        public PackageDescriptionBuilder rateIndicator(RateIndicator indicator) {
            this.rateIndicator = indicator.name();
            return this;
        }

        public PackageDescriptionBuilder rateIndicator(String indicator) {
            this.rateIndicator = indicator;
            return this;
        }

        public PackageDescriptionBuilder processingCategory(ProcessingCategory category) {
            this.processingCategory = category.name();
            return this;
        }

        public PackageDescriptionBuilder processingCategory(String category) {
            this.processingCategory = category;
            return this;
        }

        public PackageDescriptionBuilder mailingDate(LocalDate date) {
            this.mailingDate = date != null ? date.toString() : null;
            return this;
        }

        public PackageDescriptionBuilder mailingDate(String date) {
            this.mailingDate = date;
            return this;
        }
    }
}

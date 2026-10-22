package com.usps.sdk.model.labels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Visual layout and rendering options for shipping labels.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageInfo {

    @JsonProperty("imageType")
    private String imageType;

    @JsonProperty("labelType")
    private String labelType;

    @JsonProperty("receiptOption")
    private String receiptOption;

    @JsonProperty("suppressPostage")
    private Boolean suppressPostage;

    @JsonProperty("suppressMailDate")
    private Boolean suppressMailDate;

    @JsonProperty("returnLabel")
    private Boolean returnLabel;

    public static class ImageInfoBuilder {
        private String imageType = ImageType.PDF.name();
        private String labelType = LabelType.LABEL_4X6.getValue();
        private String receiptOption = "NONE";
        private Boolean suppressPostage = false;
        private Boolean suppressMailDate = false;
        private Boolean returnLabel = false;

        public ImageInfoBuilder imageType(ImageType imageType) {
            this.imageType = imageType.name();
            return this;
        }

        public ImageInfoBuilder imageType(String imageType) {
            this.imageType = imageType;
            return this;
        }

        public ImageInfoBuilder labelType(LabelType labelType) {
            this.labelType = labelType.getValue();
            return this;
        }

        public ImageInfoBuilder labelType(String labelType) {
            this.labelType = labelType;
            return this;
        }
    }
}

package io.github.c4x2.jusps.model.labels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.c4x2.jusps.exception.UspsValidationException;
import io.github.c4x2.jusps.model.common.Address;
import lombok.*;

/**
 * Request payload for creating shipping labels (POST /labels/v3/label).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateLabelRequest {

    @JsonProperty("imageInfo")
    private ImageInfo imageInfo;

    @JsonProperty("toAddress")
    private Address toAddress;

    @JsonProperty("fromAddress")
    private Address fromAddress;

    @JsonProperty("packageDescription")
    private PackageDescription packageDescription;

    @JsonIgnore
    private String paymentAuthorizationToken;

    public static class CreateLabelRequestBuilder {
        private ImageInfo imageInfo = ImageInfo.builder().build();

        public CreateLabelRequest build() {
            if (toAddress == null) {
                throw new UspsValidationException("toAddress is required to create a label");
            }
            if (fromAddress == null) {
                throw new UspsValidationException("fromAddress is required to create a label");
            }
            if (packageDescription == null) {
                throw new UspsValidationException("packageDescription is required to create a label");
            }
            return new CreateLabelRequest(imageInfo, toAddress, fromAddress, packageDescription, paymentAuthorizationToken);
        }
    }
}

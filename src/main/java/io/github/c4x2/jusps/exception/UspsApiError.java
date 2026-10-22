package io.github.c4x2.jusps.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Collections;
import java.util.List;

/**
 * Represents structured error response returned by USPS REST APIs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UspsApiError {

    @JsonProperty("apiVersion")
    private String apiVersion;

    @JsonProperty("error")
    private ErrorDetail error;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorDetail {
        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;

        @JsonProperty("errors")
        private List<ErrorItem> errors;

        public List<ErrorItem> getErrors() {
            return errors != null ? errors : Collections.emptyList();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorItem {
        @JsonProperty("title")
        private String title;

        @JsonProperty("detail")
        private String detail;

        @JsonProperty("code")
        private String code;

        @JsonProperty("source")
        private String source;

        @Override
        public String toString() {
            return (code != null ? "[" + code + "] " : "") +
                   (title != null ? title + ": " : "") +
                   (detail != null ? detail : "");
        }
    }

    @Override
    public String toString() {
        if (error != null) {
            return "UspsApiError{code='" + error.getCode() + "', message='" + error.getMessage() + "', errors=" + error.getErrors() + "}";
        }
        return "UspsApiError{}";
    }
}

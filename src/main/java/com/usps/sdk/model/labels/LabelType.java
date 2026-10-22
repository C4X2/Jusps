package com.usps.sdk.model.labels;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Dimensions and layouts for USPS labels.
 */
public enum LabelType {
    LABEL_4X6("4X6LABEL"),
    LABEL_4X4("4X4LABEL"),
    LABEL_4X6_RECEIPT("4X6LABEL_RECEIPT");

    private final String value;

    LabelType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

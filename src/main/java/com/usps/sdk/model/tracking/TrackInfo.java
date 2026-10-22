package com.usps.sdk.model.tracking;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the tracking status, delivery timestamps, and scan events for a package.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackInfo {

    @JsonProperty("trackingNumber")
    @JsonAlias({"@ID", "trackingId", "id"})
    private String trackingNumber;

    @JsonProperty("trackSummary")
    @JsonAlias({"TrackSummary", "summary"})
    private String trackSummary;

    @JsonProperty("statusCategory")
    private String statusCategory;

    @JsonProperty("statusSummary")
    private String statusSummary;

    @JsonProperty("expectedDeliveryDate")
    @JsonAlias({"predictedDeliveryDate", "estimatedDeliveryDate"})
    private String expectedDeliveryDate;

    @JsonProperty("actualDeliveryDate")
    @JsonAlias({"deliveredDate"})
    private String actualDeliveryDate;

    @JsonProperty("destinationCity")
    private String destinationCity;

    @JsonProperty("destinationState")
    private String destinationState;

    @JsonProperty("destinationZIPCode")
    @JsonAlias({"destinationZip", "destinationPostalCode"})
    private String destinationZipCode;

    @JsonProperty("originCity")
    private String originCity;

    @JsonProperty("originState")
    private String originState;

    @JsonProperty("originZIPCode")
    private String originZipCode;

    @JsonProperty("mailingDate")
    private String mailingDate;

    @JsonProperty("serviceTypeCode")
    private String serviceTypeCode;

    @JsonProperty("classOfMail")
    @JsonAlias({"mailClass"})
    private String mailClass;

    @JsonProperty("trackDetail")
    @JsonAlias({"packageEvents", "events", "scanEvents"})
    private List<TrackingEvent> trackDetail;

    public List<TrackingEvent> getTrackDetail() {
        return trackDetail != null ? trackDetail : Collections.emptyList();
    }

    /**
     * Checks if the package has been delivered.
     */
    public boolean isDelivered() {
        return (statusCategory != null && statusCategory.toUpperCase().contains("DELIVERED")) ||
               (trackSummary != null && trackSummary.toUpperCase().contains("DELIVERED")) ||
               (actualDeliveryDate != null && !actualDeliveryDate.isEmpty());
    }

    /**
     * Checks if the package is in transit.
     */
    public boolean isInTransit() {
        return statusCategory != null && statusCategory.toUpperCase().contains("TRANSIT");
    }

    /**
     * Checks if the package is in pre-shipment (label created).
     */
    public boolean isPreShipment() {
        return statusCategory != null && statusCategory.toUpperCase().contains("PRE-SHIPMENT");
    }

    /**
     * Retrieves the most recent scan event if available.
     */
    public TrackingEvent getLatestEvent() {
        if (trackDetail != null && !trackDetail.isEmpty()) {
            return trackDetail.get(0);
        }
        return null;
    }

    @Override
    public String toString() {
        return "TrackInfo{" +
                "trackingNumber='" + trackingNumber + '\'' +
                ", statusCategory='" + statusCategory + '\'' +
                ", summary='" + trackSummary + '\'' +
                ", expectedDeliveryDate='" + expectedDeliveryDate + '\'' +
                ", deliveredDate='" + actualDeliveryDate + '\'' +
                '}';
    }
}

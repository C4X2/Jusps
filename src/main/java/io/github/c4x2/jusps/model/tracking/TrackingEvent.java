package io.github.c4x2.jusps.model.tracking;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Represents a single package scan event in the postal lifecycle.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingEvent {

    @JsonProperty("eventTimestamp")
    @JsonAlias({"eventDate", "date"})
    private String eventTimestamp;

    @JsonProperty("eventTime")
    @JsonAlias({"time"})
    private String eventTime;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("eventDescription")
    @JsonAlias({"name", "event"})
    private String eventDescription;

    @JsonProperty("eventCity")
    @JsonAlias({"city"})
    private String eventCity;

    @JsonProperty("eventState")
    @JsonAlias({"state"})
    private String eventState;

    @JsonProperty("eventZIPCode")
    @JsonAlias({"ZIPCode", "zipCode"})
    private String eventZipCode;

    @JsonProperty("eventCountry")
    private String eventCountry;

    @JsonProperty("firmName")
    private String firmName;

    @JsonProperty("signee")
    @JsonAlias({"nameSignee", "authorizedAgent"})
    private String signee;

    @Override
    public String toString() {
        return "TrackingEvent{" +
                "time='" + eventTimestamp + '\'' +
                ", event='" + eventDescription + '\'' +
                ", city='" + eventCity + '\'' +
                ", state='" + eventState + '\'' +
                '}';
    }
}

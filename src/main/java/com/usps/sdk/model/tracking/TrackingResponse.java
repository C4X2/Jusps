package com.usps.sdk.model.tracking;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Top-level response container for USPS Tracking API requests.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingResponse {

    @JsonProperty("trackInfo")
    @JsonAlias({"TrackInfo", "trackResults", "TrackResults", "trackingInfo"})
    private Object rawTrackInfo;

    /**
     * Resolves the primary TrackInfo object from the response.
     */
    @JsonIgnore
    public TrackInfo getTrackInfo() {
        if (rawTrackInfo instanceof TrackInfo ti) {
            return ti;
        }
        if (rawTrackInfo instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof TrackInfo ti) {
                return ti;
            }
            try {
                return com.usps.sdk.http.JsonMapper.fromJson(
                        com.usps.sdk.http.JsonMapper.toJson(first), TrackInfo.class);
            } catch (Exception ignored) {}
        }
        if (rawTrackInfo instanceof java.util.Map<?, ?> map) {
            try {
                // Check if wrapped in TrackInfo
                if (map.containsKey("TrackInfo")) {
                    return com.usps.sdk.http.JsonMapper.fromJson(
                            com.usps.sdk.http.JsonMapper.toJson(map.get("TrackInfo")), TrackInfo.class);
                }
                if (map.containsKey("trackInfo")) {
                    return com.usps.sdk.http.JsonMapper.fromJson(
                            com.usps.sdk.http.JsonMapper.toJson(map.get("trackInfo")), TrackInfo.class);
                }
                return com.usps.sdk.http.JsonMapper.fromJson(
                        com.usps.sdk.http.JsonMapper.toJson(map), TrackInfo.class);
            } catch (Exception ignored) {}
        }
        return null;
    }

    @Override
    public String toString() {
        TrackInfo ti = getTrackInfo();
        return "TrackingResponse{" + (ti != null ? ti.toString() : "empty") + "}";
    }
}

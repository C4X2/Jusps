package com.usps.sdk.model.locations;

import com.usps.sdk.exception.UspsValidationException;
import com.usps.sdk.model.common.MailClass;
import lombok.*;

/**
 * Request parameters for discovering drop-off / destination entry facilities (GET /locations/v3/dropoff-locations).
 */
@Getter
@AllArgsConstructor
@Builder
public class DropOffLocationsRequest {

    private final String destinationZIPCode;
    private final String mailClass;
    private final String destinationEntryFacilityType;

    public static class DropOffLocationsRequestBuilder {
        public DropOffLocationsRequestBuilder mailClass(MailClass mailClass) {
            this.mailClass = mailClass.name();
            return this;
        }

        public DropOffLocationsRequestBuilder mailClass(String mailClass) {
            this.mailClass = mailClass;
            return this;
        }

        public DropOffLocationsRequest build() {
            if (destinationZIPCode == null || destinationZIPCode.trim().isEmpty()) {
                throw new UspsValidationException("destinationZIPCode is required");
            }
            return new DropOffLocationsRequest(destinationZIPCode, mailClass, destinationEntryFacilityType);
        }
    }
}

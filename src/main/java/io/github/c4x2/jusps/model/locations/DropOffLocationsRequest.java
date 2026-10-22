package io.github.c4x2.jusps.model.locations;

import io.github.c4x2.jusps.exception.UspsValidationException;
import io.github.c4x2.jusps.model.common.MailClass;
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

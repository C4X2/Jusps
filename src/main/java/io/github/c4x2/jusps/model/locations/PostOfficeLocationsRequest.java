package io.github.c4x2.jusps.model.locations;

import io.github.c4x2.jusps.exception.UspsValidationException;
import lombok.*;

/**
 * Request parameters for locating Post Office locations within a radius (GET /locations/v3/post-office-locations).
 */
@Getter
@AllArgsConstructor
@Builder
public class PostOfficeLocationsRequest {

    private final String zipCode;
    private final int radius;

    public static class PostOfficeLocationsRequestBuilder {
        private int radius = 5;

        public PostOfficeLocationsRequest build() {
            if (zipCode == null || zipCode.trim().isEmpty()) {
                throw new UspsValidationException("zipCode is required");
            }
            if (radius <= 0) {
                throw new UspsValidationException("radius must be positive");
            }
            return new PostOfficeLocationsRequest(zipCode, radius);
        }
    }
}

package io.github.c4x2.jusps.model.standards;

import io.github.c4x2.jusps.exception.UspsValidationException;
import io.github.c4x2.jusps.model.common.MailClass;
import lombok.*;

import java.time.LocalDate;

/**
 * Request parameters for delivery standard estimates (GET /service-standards/v3/estimates).
 */
@Getter
@AllArgsConstructor
@Builder
public class ServiceStandardRequest {

    private final String originZIPCode;
    private final String destinationZIPCode;
    private final String acceptanceDate;
    private final String mailClass;
    private final String destinationType;
    private final String serviceTypeCodes;

    public static class ServiceStandardRequestBuilder {
        private String acceptanceDate = LocalDate.now().toString();

        public ServiceStandardRequestBuilder acceptanceDate(LocalDate date) {
            this.acceptanceDate = date != null ? date.toString() : null;
            return this;
        }

        public ServiceStandardRequestBuilder acceptanceDate(String date) {
            this.acceptanceDate = date;
            return this;
        }

        public ServiceStandardRequestBuilder mailClass(MailClass mailClass) {
            this.mailClass = mailClass.name();
            return this;
        }

        public ServiceStandardRequestBuilder mailClass(String mailClass) {
            this.mailClass = mailClass;
            return this;
        }

        public ServiceStandardRequest build() {
            if (originZIPCode == null || originZIPCode.trim().isEmpty()) {
                throw new UspsValidationException("originZIPCode is required for service standards estimate");
            }
            if (destinationZIPCode == null || destinationZIPCode.trim().isEmpty()) {
                throw new UspsValidationException("destinationZIPCode is required for service standards estimate");
            }
            return new ServiceStandardRequest(originZIPCode, destinationZIPCode, acceptanceDate, mailClass, destinationType, serviceTypeCodes);
        }
    }
}

package gov.va.api.lighthouse.mpi;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import org.hl7.v3.II;
import org.hl7.v3.PRPAIN201306UV02;

@Builder
public class PatientIdTransformer {
  PRPAIN201306UV02 mpi;

  boolean anyBlank(Object... values) {
    for (Object v : values) {
      if (isBlank(v)) {
        return true;
      }
    }
    return false;
  }

  boolean isBlank(Object value) {
    if (value instanceof Collection<?>) {
      return ((Collection<?>) value).isEmpty();
    }
    return value == null;
  }

  List<PatientIdentifierSegment> toPatientIdList() {
    // Verify no null/empty values all the way down to the ids list we want
    // We'll get index 0 because when searching by a national icn, there should only ever be 1
    // patient
    if (anyBlank(
        mpi.getControlActProcess(),
        mpi.getControlActProcess().getSubject(),
        mpi.getControlActProcess().getSubject().get(0).getRegistrationEvent(),
        mpi.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1(),
        mpi.getControlActProcess()
            .getSubject()
            .get(0)
            .getRegistrationEvent()
            .getSubject1()
            .getPatient(),
        mpi.getControlActProcess()
            .getSubject()
            .get(0)
            .getRegistrationEvent()
            .getSubject1()
            .getPatient()
            .getId())) {
      return List.of();
    }
    return mpi
        .getControlActProcess()
        .getSubject()
        .get(0)
        .getRegistrationEvent()
        .getSubject1()
        .getPatient()
        .getId()
        .stream()
        .map(II::getExtension)
        .filter(Objects::nonNull)
        .map(PatientIdentifierSegment::parse)
        .collect(Collectors.toList());
  }
}

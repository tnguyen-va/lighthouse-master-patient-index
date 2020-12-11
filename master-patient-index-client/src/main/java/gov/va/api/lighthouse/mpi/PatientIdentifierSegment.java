package gov.va.api.lighthouse.mpi;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PatientIdentifierSegment {
  @NonNull String identifier;

  /**
   * National Identifier (NI) Patient Identifier (PI) Employee Identifier (EI) Patient Number (PN).
   */
  @NonNull String identifierType;

  /** Station Id. */
  String assigningLocation;

  String assigningAuthority;

  /**
   * A-Active, D-Deprecated from a Duplicate, M-Deprecated from a Mismatch, U-Deprecated from an
   * Unlink, H-Deprecated from a Local Merge, PCE-Pending Cat Edit correlations.
   */
  String identifierStatus;

  private static String getOrNull(String[] array, int index) {
    return index >= array.length ? null : array[index];
  }

  /** Parse an HL7 identifier string. E.g. 100005750^PI^500^USVHA^A */
  public static PatientIdentifierSegment parse(String identifierSegmentString) {
    String[] segmentParts = identifierSegmentString.split("\\^");
    return PatientIdentifierSegment.builder()
        .identifier(getOrNull(segmentParts, 0))
        .identifierType(getOrNull(segmentParts, 1))
        .assigningLocation(getOrNull(segmentParts, 2))
        .assigningAuthority(getOrNull(segmentParts, 3))
        .identifierStatus(getOrNull(segmentParts, 4))
        .build();
  }

  /** Return true if Active AND Patient Identifier AND assigned by VHA. */
  public boolean isVistaSite() {
    return "A".equals(identifierStatus())
        && "PI".equals(identifierType())
        && "USVHA".equals(assigningAuthority());
  }

  /** Return an identifier string suitable for the HL7 messages. E.g., 00005750^PI^500^USVHA^A */
  public String toIdentifierString() {
    StringBuilder s = new StringBuilder(identifier() + "^" + identifierType());
    // Values are order sensitive
    if (assigningLocation() == null) {
      return s.toString();
    }
    s.append("^").append(assigningLocation());
    if (assigningAuthority() == null) {
      return s.toString();
    }
    s.append("^").append(assigningAuthority());
    if (identifierStatus() == null) {
      return s.toString();
    }
    return s.append("^").append(identifierStatus()).toString();
  }
}

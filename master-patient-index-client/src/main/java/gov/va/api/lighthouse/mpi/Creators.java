package gov.va.api.lighthouse.mpi;

import lombok.experimental.UtilityClass;
import org.hl7.v3.CS;

@UtilityClass
public final class Creators {

  /** Return a CS with the given code or null if code is null. */
  public static CS csWithCode(String code) {
    if (code == null) {
      return null;
    }
    CS cs = CS.cSBuilder().build();
    cs.setCode(code);
    return cs;
  }
}

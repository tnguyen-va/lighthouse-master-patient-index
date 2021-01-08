package gov.va.api.lighthouse.mpi;

import lombok.experimental.UtilityClass;
import org.hl7.v3.CE;
import org.hl7.v3.CS;
import org.hl7.v3.ST;
import org.hl7.v3.TEL;

@UtilityClass
public final class Creators {
  /** Return a CE with the given code or null if code is null. */
  public static CE ceWithCode(String code) {
    if (code == null) {
      return null;
    }
    CE ce = CE.cEBuilder().build();
    ce.setCode(code);
    return ce;
  }

  /** Return a CS with the given code or null if code is null. */
  public static CS csWithCode(String code) {
    if (code == null) {
      return null;
    }
    CS cs = CS.cSBuilder().build();
    cs.setCode(code);
    return cs;
  }

  /** Return a ST with the given content or null if content is null. */
  public static ST stWithContent(String content) {
    if (content == null) {
      return null;
    }
    ST st = ST.sTBuilder().build();
    st.getContent().add(content);
    return st;
  }

  /** Return a TEL with the given code or null if code is null. */
  public static TEL telWithValue(String value) {
    if (value == null) {
      return null;
    }
    TEL tel = TEL.tELBuilder().build();
    tel.setValue(value);
    return tel;
  }
}

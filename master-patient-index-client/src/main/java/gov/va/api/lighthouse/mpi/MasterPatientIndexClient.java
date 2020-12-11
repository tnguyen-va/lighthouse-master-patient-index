package gov.va.api.lighthouse.mpi;

import org.hl7.v3.PRPAIN201310UV02;

public interface MasterPatientIndexClient {
  PRPAIN201310UV02 request1309ByIcn(String icn);
}

package gov.va.api.lighthouse.mpi;

import static gov.va.api.lighthouse.mpi.Creators.csWithCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import lombok.Builder;
import lombok.Value;
import org.hl7.v3.ActClassControlAct;
import org.hl7.v3.CD;
import org.hl7.v3.COCTMT090100UV01AssignedPerson;
import org.hl7.v3.COCTMT090100UV01Person;
import org.hl7.v3.CommunicationFunctionType;
import org.hl7.v3.EN;
import org.hl7.v3.EntityClassDevice;
import org.hl7.v3.II;
import org.hl7.v3.MCCIMT000100UV01Agent;
import org.hl7.v3.MCCIMT000100UV01Device;
import org.hl7.v3.MCCIMT000100UV01Organization;
import org.hl7.v3.MCCIMT000100UV01Receiver;
import org.hl7.v3.MCCIMT000100UV01Sender;
import org.hl7.v3.PRPAIN201309UV02;
import org.hl7.v3.PRPAIN201309UV02QUQIMT021001UV01ControlActProcess;
import org.hl7.v3.PRPAMT201307UV02ParameterList;
import org.hl7.v3.PRPAMT201307UV02PatientIdentifier;
import org.hl7.v3.PRPAMT201307UV02QueryByParameter;
import org.hl7.v3.QUQIMT021001UV01DataEnterer;
import org.hl7.v3.ST;
import org.hl7.v3.TS;
import org.hl7.v3.XActMoodIntentEvent;

@Value
@Builder
public class Mpi1309Creator {
  @Builder.Default String initialQuantity = "10";
  @Builder.Default String responseElementGroupIdExtension = "PV";
  @Builder.Default boolean retrieveRelationships = true;
  @Builder.Default String processingCode = "T";
  @Builder.Default String processingModeCode = "T";

  MpiConfig config;

  String icn;

  JAXBElement<MCCIMT000100UV01Agent> asAgent() {
    return new JAXBElement<>(
        new QName("urn:hl7-org:v3", "asAgent"),
        MCCIMT000100UV01Agent.class,
        MCCIMT000100UV01Agent.builder()
            .classCode(List.of("AGNT"))
            .representedOrganization(
                new JAXBElement<>(
                    new QName("urn:hl7-org:v3", "representedOrganization"),
                    MCCIMT000100UV01Organization.class,
                    MCCIMT000100UV01Organization.builder()
                        .classCode("ORG")
                        .determinerCode("INSTANCE")
                        .id(
                            List.of(
                                II.iIBuilder()
                                    .root("2.16.840.1.113883.4.349")
                                    .extension(config.getAsAgentId())
                                    .build()))
                        .build()))
            .build());
  }

  PRPAIN201309UV02 asSoapRequest() {
    PRPAIN201309UV02 message =
        PRPAIN201309UV02.pRPAIN201309UV02Builder().itsVersion("XML_1.0").build();

    message.setId(requesterId());
    message.setCreationTime(TS.tSBuilder().value(creationTime()).build());
    message.setVersionCode(csWithCode("4.0"));
    message.setInteractionId(II.iIBuilder().root("2.16.840.1.113883.1.6").build());
    message.setProcessingCode(csWithCode(processingCode()));
    message.setProcessingModeCode(csWithCode(processingModeCode()));
    message.setAcceptAckCode(csWithCode("AL"));
    message.getReceiver().add(receiver());
    message.setSender(sender());
    message.setControlActProcess(
        PRPAIN201309UV02QUQIMT021001UV01ControlActProcess.builder()
            .classCode(ActClassControlAct.CACT)
            .moodCode(XActMoodIntentEvent.EVN)
            .code(
                CD.cDBuilder()
                    .code("PRPA_TE201309UV02")
                    .codeSystem("2.16.840.1.113883.1.6")
                    .build())
            .dataEnterer(dataEnterer())
            .queryByParameter(queryByParameter())
            .build());

    return message;
  }

  private String creationTime() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    LocalDateTime dateTime = LocalDateTime.now(ZoneId.systemDefault());
    return dtf.format(dateTime);
  }

  private List<QUQIMT021001UV01DataEnterer> dataEnterer() {
    return List.of(
        QUQIMT021001UV01DataEnterer.builder()
            .contextControlCode("AP")
            .typeCode(List.of("ENT"))
            .assignedPerson(
                COCTMT090100UV01AssignedPerson.builder()
                    .classCode("ASSIGNED")
                    .id(
                        List.of(
                            II.iIBuilder()
                                .root("2.16.840.1.113883.4.349")
                                .extension(config.getAsAgentId())
                                .build()))
                    .assignedPerson(
                        new JAXBElement<>(
                            new QName("assignedPerson"),
                            COCTMT090100UV01Person.class,
                            COCTMT090100UV01Person.builder()
                                .classCode(List.of("PSN"))
                                .determinerCode("INSTANCE")
                                .name(
                                    List.of(
                                        EN.eNBuilder().content(getAssignedPersonValue()).build()))
                                .build()))
                    .build())
            .build());
  }

  private List<Serializable> getAssignedPersonValue() {
    String userId = config.getUserId();
    if (userId.contains(" ")) {
      return List.of(
          new JAXBElement<>(
              new QName("urn:hl7-org:v3", "given"),
              String.class,
              EN.class,
              getTokenFrom(userId, 0)),
          new JAXBElement<>(
              new QName("urn:hl7-org:v3", "family"),
              String.class,
              EN.class,
              getTokenFrom(userId, 1)));
    } else {
      return List.of(
          new JAXBElement<>(
              new QName("urn:hl7-org:v3", "family"), String.class, EN.class, userId.trim()));
    }
  }

  private String getTokenFrom(String id, int index) {
    StringTokenizer tokenizer = new StringTokenizer(id, " ");
    String token = tokenizer.nextToken().trim();
    for (int i = 0; i < index; i++) {
      token = tokenizer.nextToken().trim();
    }
    return token;
  }

  private JAXBElement<PRPAMT201307UV02QueryByParameter> queryByParameter() {
    String icnIdentifierSegment =
        PatientIdentifierSegment.builder()
            .identifier(icn())
            .identifierType("NI")
            .assigningLocation("200M")
            .assigningAuthority("USVHA")
            .build()
            .toIdentifierString();
    ST semanticsText = ST.sTBuilder().build();
    semanticsText.getContent().add("Patient.Id");
    return new JAXBElement<>(
        new QName("urn:hl7-org:v3", "queryByParameter"),
        PRPAMT201307UV02QueryByParameter.class,
        PRPAMT201307UV02QueryByParameter.builder()
            // ToDo Should this be unique?
            .queryId(
                II.iIBuilder()
                    .root("1.2.840.114350.1.13.99999.4567.34")
                    .extension("MY_TST_9703")
                    .build())
            .statusCode(csWithCode("new"))
            .responsePriorityCode(csWithCode("I"))
            .parameterList(
                PRPAMT201307UV02ParameterList.builder()
                    .patientIdentifier(
                        List.of(
                            PRPAMT201307UV02PatientIdentifier.builder()
                                .value(
                                    List.of(
                                        II.iIBuilder()
                                            .root("2.16.840.1.113883.4.349")
                                            .extension(icnIdentifierSegment)
                                            .build()))
                                .semanticsText(semanticsText)
                                .build()))
                    .build())
            .build());
  }

  private MCCIMT000100UV01Receiver receiver() {
    return MCCIMT000100UV01Receiver.builder()
        .typeCode(CommunicationFunctionType.RCV)
        .device(
            MCCIMT000100UV01Device.builder()
                .classCode(EntityClassDevice.DEV)
                .determinerCode("INSTANCE")
                .id(List.of(II.iIBuilder().root("2.16.840.1.113883.4.349").build()))
                .build())
        .build();
  }

  private II requesterId() {
    // ToDo in the wild, we'll want to log this id for request tracking
    String uniqueId = "MCID-DVP_" + UUID.randomUUID();
    return II.iIBuilder().root("1.2.840.114350.1.13.0.1.7.1.1").extension(uniqueId).build();
  }

  private MCCIMT000100UV01Sender sender() {
    return MCCIMT000100UV01Sender.builder()
        .typeCode(CommunicationFunctionType.SND)
        .device(
            MCCIMT000100UV01Device.builder()
                .classCode(EntityClassDevice.DEV)
                .determinerCode("INSTANCE")
                .id(
                    List.of(
                        II.iIBuilder()
                            .extension(config.getIntegrationProcessId())
                            .root("2.16.840.1.113883.3.42.10001.100001.12")
                            .build()))
                .asAgent(asAgent())
                .build())
        .build();
  }
}

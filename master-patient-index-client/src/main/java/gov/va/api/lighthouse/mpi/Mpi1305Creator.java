package gov.va.api.lighthouse.mpi;

import static gov.va.api.lighthouse.mpi.Creators.ceWithCode;
import static gov.va.api.lighthouse.mpi.Creators.csWithCode;
import static gov.va.api.lighthouse.mpi.Creators.stWithContent;
import static gov.va.api.lighthouse.mpi.Creators.telWithValue;
import static java.util.Collections.singletonList;

import io.micrometer.core.instrument.util.StringUtils;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import lombok.Builder;
import lombok.Value;
import org.hl7.v3.ActClassControlAct;
import org.hl7.v3.CD;
import org.hl7.v3.COCTMT090100UV01AssignedPerson;
import org.hl7.v3.COCTMT090100UV01Person;
import org.hl7.v3.COCTMT150000UV02Organization;
import org.hl7.v3.CommunicationFunctionType;
import org.hl7.v3.EN;
import org.hl7.v3.EntityClassDevice;
import org.hl7.v3.II;
import org.hl7.v3.INT;
import org.hl7.v3.IVLTS;
import org.hl7.v3.MCCIMT000100UV01Agent;
import org.hl7.v3.MCCIMT000100UV01Device;
import org.hl7.v3.MCCIMT000100UV01Organization;
import org.hl7.v3.MCCIMT000100UV01Receiver;
import org.hl7.v3.MCCIMT000100UV01Sender;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201305UV02QUQIMT021001UV01ControlActProcess;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectAdministrativeGender;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectBirthTime;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectId;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectName;
import org.hl7.v3.PRPAMT201306UV02ParameterList;
import org.hl7.v3.PRPAMT201306UV02QueryByParameter;
import org.hl7.v3.QUQIMT021001UV01DataEnterer;
import org.hl7.v3.TS;
import org.hl7.v3.XActMoodIntentEvent;

@Value
@Builder
public class Mpi1305Creator {
  @Builder.Default String initialQuantity = "10";
  @Builder.Default String responseElementGroupIdExtension = "PV";
  @Builder.Default boolean retrieveRelationships = true;
  @Builder.Default String processingCode = "T";
  @Builder.Default String processingModeCode = "T";

  MpiConfig config;

  Mpi1305RequestAttributes attributes;

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

  PRPAIN201305UV02 asSoapRequest() {
    PRPAIN201305UV02 message = PRPAIN201305UV02.pRPAIN201305UV02Builder().build();
    message.setId(requesterId());
    message.setCreationTime(TS.tSBuilder().value(creationTime()).build());
    message.setVersionCode(csWithCode("4.0"));
    message.setInteractionId(
        II.iIBuilder().root("2.16.840.1.113883.1.6").extension("PRPA_IN201305UV02").build());
    message.setProcessingCode(csWithCode(processingCode()));
    message.setProcessingModeCode(csWithCode(processingCode()));
    message.setAcceptAckCode(csWithCode("AL"));
    message.getReceiver().add(receiver());
    message.setSender(sender());
    message.setControlActProcess(
        PRPAIN201305UV02QUQIMT021001UV01ControlActProcess.builder()
            .classCode(ActClassControlAct.CACT)
            .moodCode(XActMoodIntentEvent.EVN)
            .code(
                CD.cDBuilder()
                    .code("PRPA_TE201305UV02")
                    .codeSystem("2.16.840.1.113883.1.6")
                    .build())
            .dataEnterer(dataEnterer())
            .queryByParameter(queryByParameter())
            .build());
    return message;
  }

  private List<IVLTS> birthTime() {
    IVLTS birthTime = IVLTS.iVLTSBuilder().build();
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    birthTime.setValue(dateFormat.format(attributes.getBirthTime()));
    return singletonList(birthTime);
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
                            new QName("urn:hl7-org:v3", "assignedPerson"),
                            COCTMT090100UV01Person.class,
                            COCTMT090100UV01Person.builder()
                                .classCode(List.of("PSN"))
                                .determinerCode("INSTANCE")
                                .name(
                                    List.of(
                                        EN.eNBuilder().content(getAssignedPersonValue()).build()))
                                .build()))
                    .representedOrganization(
                        new JAXBElement<>(
                            new QName("urn:hl7-org:v3", "representedOrganization"),
                            COCTMT150000UV02Organization.class,
                            COCTMT150000UV02Organization.builder()
                                .classCode("ORG")
                                .determinerCode("INSTANCE")
                                .id(
                                    List.of(
                                        II.iIBuilder()
                                            .root("2.16.840.1.113883.4.349")
                                            .extension("UID")
                                            .build()))
                                .code(ceWithCode("KEY"))
                                .telecom(List.of(telWithValue("10.226.86.189")))
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

  private List<Serializable> nameList() {
    List<Serializable> nameList = new ArrayList<>();
    if (StringUtils.isNotBlank(attributes.getFirstName())) {
      nameList.add(
          new JAXBElement<>(
              new QName("urn:hl7-org:v3", "given"),
              String.class,
              EN.class,
              attributes.getFirstName()));
    }
    // Only add second 'given' if it has a value
    if (StringUtils.isNotBlank(attributes.getMiddleName())) {
      nameList.add(
          new JAXBElement<>(
              new QName("urn:hl7-org:v3", "given"),
              String.class,
              EN.class,
              attributes.getMiddleName()));
    }
    if (StringUtils.isNotBlank(attributes.getLastName())) {
      nameList.add(
          new JAXBElement<>(
              new QName("urn:hl7-org:v3", "family"),
              String.class,
              EN.class,
              attributes.getLastName()));
    }
    return nameList;
  }

  private PRPAMT201306UV02ParameterList parameters() {
    PRPAMT201306UV02ParameterList.PRPAMT201306UV02ParameterListBuilder paramBuilder =
        PRPAMT201306UV02ParameterList.builder();
    if (attributes != null) {
      if (attributes.getSsn() != null) {
        paramBuilder.livingSubjectId(
            singletonList(
                PRPAMT201306UV02LivingSubjectId.builder()
                    .value(
                        singletonList(
                            II.iIBuilder()
                                .root("2.16.840.1.113883.4.1")
                                .extension(attributes.getSsn())
                                .build()))
                    .semanticsText(stWithContent("SSN"))
                    .build()));
      }
      if (attributes.getBirthTime() != null) {
        paramBuilder.livingSubjectBirthTime(
            singletonList(
                PRPAMT201306UV02LivingSubjectBirthTime.builder()
                    .value(birthTime())
                    .semanticsText(stWithContent("LivingSubject.birthTime"))
                    .build()));
      }
      paramBuilder.livingSubjectName(
          singletonList(
              PRPAMT201306UV02LivingSubjectName.builder()
                  .value(
                      singletonList(
                          EN.eNBuilder().use(singletonList("L")).content(nameList()).build()))
                  .build()));
      if (StringUtils.isNotBlank(attributes.getGender())) {
        paramBuilder.livingSubjectAdministrativeGender(
            singletonList(
                PRPAMT201306UV02LivingSubjectAdministrativeGender.builder()
                    .value(singletonList(ceWithCode(attributes.getGender())))
                    .semanticsText(stWithContent("LivingSubject.administrativeGender"))
                    .build()));
      }
    }
    return paramBuilder.build();
  }

  private JAXBElement<PRPAMT201306UV02QueryByParameter> queryByParameter() {
    return new JAXBElement<>(
        new QName("urn:hl7-org:v3", "queryByParameter"),
        PRPAMT201306UV02QueryByParameter.class,
        PRPAMT201306UV02QueryByParameter.builder()
            .queryId(II.iIBuilder().root("2.16.840.1.113883.3.933").extension("18204").build())
            .statusCode(csWithCode("new"))
            .modifyCode(csWithCode(retrieveRelationships ? "MVI.COMP1.RMS" : "MVI.COMP1"))
            .responseElementGroupId(
                List.of(
                    II.iIBuilder()
                        .extension(responseElementGroupIdExtension)
                        .root("2.16.840.1.113883.4.349")
                        .build()))
            .initialQuantity(INT.iNTBuilder().value(new BigInteger(initialQuantity)).build())
            .parameterList(parameters())
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
    return II.iIBuilder().root("2.16.840.1.113883.3.933").build();
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
                            .root("2.16.840.1.113883.3.933")
                            .build()))
                .asAgent(asAgent())
                .build())
        .build();
  }
}

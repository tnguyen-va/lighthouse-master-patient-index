package gov.va.api.lighthouse.mpi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("mpi")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MpiConfig {
  String url;
  String wsdlLocation;
  String keystorePath;
  String keystorePassword;
  String keyAlias;
  String truststorePath;
  String truststorePassword;
  String userId;
  String integrationProcessId; // e.g. 200DVPE
  String asAgentId; // e.g. 200DVPG
}

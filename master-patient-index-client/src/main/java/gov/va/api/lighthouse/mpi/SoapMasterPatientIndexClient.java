package gov.va.api.lighthouse.mpi;

import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import gov.va.oit.oed.vaww.VAIdM;
import gov.va.oit.oed.vaww.VAIdMPort;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.xml.ws.BindingProvider;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.PRPAIN201309UV02;
import org.hl7.v3.PRPAIN201310UV02;
import org.springframework.util.ResourceUtils;

@Getter
@Slf4j
public class SoapMasterPatientIndexClient implements MasterPatientIndexClient {
  private final SSLContext sslContext;

  private final MpiConfig config;

  private SoapMasterPatientIndexClient(MpiConfig config) {
    this.config = config;
    this.sslContext = createSslContext();
    /*
     * Temporary, this should be replaced with configuration that alters the SSL socket factory per
     * port. The truststore and keystore should also be re-used. However, to move forward with
     * integration testing, this is deferred.
     * Today is Dec 11, 2020.
     */
    javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
  }

  public static SoapMasterPatientIndexClient of(MpiConfig config) {
    return new SoapMasterPatientIndexClient(config);
  }

  /** Configure SSL for SOAP communication with MPI. */
  @SneakyThrows
  private SSLContext createSslContext() {
    try (InputStream keystoreInputStream =
            ResourceUtils.getURL(config.getKeystorePath()).openStream();
        InputStream truststoreInputStream =
            ResourceUtils.getURL(config.getTruststorePath()).openStream()) {
      // Keystore
      KeyStore keystore = KeyStore.getInstance("JKS");
      keystore.load(keystoreInputStream, config.getKeystorePassword().toCharArray());
      KeyManagerFactory kmf =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(keystore, config.getKeystorePassword().toCharArray());
      final X509KeyManager origKm = (X509KeyManager) kmf.getKeyManagers()[0];
      // Truststore
      KeyStore truststore = KeyStore.getInstance("JKS");
      truststore.load(truststoreInputStream, config.getTruststorePassword().toCharArray());
      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(truststore);
      // SSL Context
      SSLContext sslContext = SSLContext.getInstance("TLS");
      X509KeyManager keyManager =
          new X509KeyManager() {
            @Override
            public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
              return config.getKeyAlias();
            }

            @Override
            public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
              return origKm.chooseServerAlias(keyType, issuers, socket);
            }

            @Override
            public X509Certificate[] getCertificateChain(String alias) {
              return origKm.getCertificateChain(alias);
            }

            @Override
            public String[] getClientAliases(String keyType, Principal[] issuers) {
              return origKm.getClientAliases(keyType, issuers);
            }

            @Override
            public PrivateKey getPrivateKey(String alias) {
              return origKm.getPrivateKey(alias);
            }

            @Override
            public String[] getServerAliases(String keyType, Principal[] issuers) {
              return origKm.getServerAliases(keyType, issuers);
            }
          };
      sslContext.init(
          new KeyManager[] {keyManager},
          trustManagerFactory.getTrustManagers(),
          new SecureRandom());
      return sslContext;
    } catch (IOException | GeneralSecurityException e) {
      log.error("Failed to create SSL context", e);
      throw e;
    }
  }

  @SneakyThrows
  private VAIdMPort port() {
    SSLSocketFactory socketFactory = sslContext().getSocketFactory();
    try {
      VAIdMPort port = new VAIdM(new URL(config.getWsdlLocation())).getVAIdMPort();
      BindingProvider bp = (BindingProvider) port;
      bp.getRequestContext()
          .put(com.sun.xml.ws.developer.JAXWSProperties.SSL_SOCKET_FACTORY, socketFactory);
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.getUrl());
      return port;
    } catch (InaccessibleWSDLException e) {
      log.error("WSDL is inaccessible: {}", e.getMessage());
      e.getErrors().forEach(t -> log.error("Reason {}", t.getMessage()));
      throw e;
    } catch (MalformedURLException e) {
      log.error("Failed to create port for {}", config.getWsdlLocation(), e);
      throw e;
    }
  }

  /** Make a 1305 request. */
  @Override
  public PRPAIN201306UV02 request1305ByAttributes(Mpi1305RequestAttributes attributes) {
    PRPAIN201305UV02 mvi1305RequestBody =
        Mpi1305Creator.builder().config(config).attributes(attributes).build().asSoapRequest();
    return port().prpaIN201305UV02(mvi1305RequestBody);
  }

  /** Make a 1309 request. */
  @Override
  public PRPAIN201310UV02 request1309ByIcn(String patientIcn) {
    PRPAIN201309UV02 mvi1309RequestBody =
        Mpi1309Creator.builder().config(config).icn(patientIcn).build().asSoapRequest();
    return port().prpaIN201309UV02(mvi1309RequestBody);
  }
}

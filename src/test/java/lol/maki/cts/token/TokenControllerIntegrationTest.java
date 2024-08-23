package lol.maki.cts.token;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import lol.maki.cts.CertUtils;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"logging.level.org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider=info",
		"logging.level.web=debug", "jwt.audience=test", "jwt.public-key=classpath:rsa/public_key",
		"jwt.private-key=classpath:rsa/private_key", "spring.profiles.active=mtls", })
public class TokenControllerIntegrationTest {

	@Autowired
	RestClient.Builder restClientBuilder;

	@LocalServerPort
	int port;

	@Test
	void testWithValidCertificate() throws Exception {
		KeyStore keyStore = CertUtils.createKeyStore("self-signed/client.key", "self-signed/client.crt", "password");
		KeyStore trustStore = CertUtils.createTrustStore("self-signed/ca.crt");
		HttpClient httpClient = createHttpClient(keyStore, "password", trustStore);
		RestClient restClient = this.restClientBuilder.requestFactory(new JdkClientHttpRequestFactory(httpClient))
			.baseUrl("https://localhost:" + port)
			.build();
		ResponseEntity<String> response = restClient.post().uri("/token").retrieve().toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotEmpty();
		JWTClaimsSet jwtClaimsSet = JWTParser.parse(response.getBody()).getJWTClaimsSet();
		assertThat(jwtClaimsSet.getIssuer()).isEqualTo("https://localhost:" + port);
		assertThat(jwtClaimsSet.getAudience()).containsExactly("test");
		assertThat(jwtClaimsSet.getSubject()).isEqualTo(
				"4b84793c-f3ea-4a55-92b7-942726aac163:6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3:02756191-d869-4806-9717-a6eec5142e8a");
		assertThat(jwtClaimsSet.getStringClaim("org_guid")).isEqualTo("4b84793c-f3ea-4a55-92b7-942726aac163");
		assertThat(jwtClaimsSet.getStringClaim("space_guid")).isEqualTo("6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3");
		assertThat(jwtClaimsSet.getStringClaim("app_guid")).isEqualTo("02756191-d869-4806-9717-a6eec5142e8a");
	}

	@Test
	void testWithMissingCertificate() {
		KeyStore keyStore = CertUtils.createEmptyKeyStore();
		KeyStore trustStore = CertUtils.createTrustStore("self-signed/ca.crt");
		HttpClient httpClient = createHttpClient(keyStore, "password", trustStore);
		RestClient restClient = this.restClientBuilder.requestFactory(new JdkClientHttpRequestFactory(httpClient))
			.baseUrl("https://localhost:" + port)
			.build();
		ResponseEntity<String> response = restClient.post().uri("/token").retrieve().toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	static HttpClient createHttpClient(KeyStore keyStore, String keyStorePassword, KeyStore trustStore) {
		try {
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, keyStorePassword.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			return HttpClient.newBuilder().sslContext(sslContext).build();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class Config {

		@Bean
		public RestClientCustomizer restClientBuilderCustomizer() throws Exception {
			return restClientBuilder -> restClientBuilder.defaultStatusHandler(new DefaultResponseErrorHandler() {
				@Override
				protected void handleError(ClientHttpResponse response, HttpStatusCode statusCode) throws IOException {
					// NO-OP
				}
			});
		}

	}

}

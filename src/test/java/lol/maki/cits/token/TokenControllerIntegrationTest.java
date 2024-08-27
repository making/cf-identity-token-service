package lol.maki.cits.token;

import java.io.IOException;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientSsl;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"logging.level.org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider=info",
		"logging.level.web=debug", "jwt.audience=test", "jwt.public-key=classpath:rsa/public_key",
		"jwt.private-key=classpath:rsa/private_key", "spring.profiles.active=mtls",
		"spring.ssl.bundle.pem.client.keystore.certificate=classpath:self-signed/client.crt",
		"spring.ssl.bundle.pem.client.keystore.private-key=classpath:self-signed/client.key",
		"spring.ssl.bundle.pem.client.truststore.certificate=classpath:self-signed/ca.crt",
		"spring.ssl.bundle.pem.cacert.truststore.certificate=classpath:self-signed/ca.crt" })
public class TokenControllerIntegrationTest {

	@Autowired
	RestClient.Builder restClientBuilder;

	@Autowired
	RestClientSsl clientSsl;

	@LocalServerPort
	int port;

	@Test
	void testWithValidCertificate() throws Exception {
		RestClient restClient = this.restClientBuilder.apply(this.clientSsl.fromBundle("client"))
			.baseUrl("https://localhost:" + this.port)
			.build();
		ResponseEntity<String> response = restClient.post().uri("/token").retrieve().toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotEmpty();
		JWTClaimsSet jwtClaimsSet = JWTParser.parse(response.getBody()).getJWTClaimsSet();
		assertThat(jwtClaimsSet.getIssuer()).isEqualTo("https://localhost:" + this.port);
		assertThat(jwtClaimsSet.getAudience()).containsExactly("test");
		assertThat(jwtClaimsSet.getSubject()).isEqualTo(
				"4b84793c-f3ea-4a55-92b7-942726aac163:6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3:02756191-d869-4806-9717-a6eec5142e8a");
		assertThat(jwtClaimsSet.getStringClaim("org_guid")).isEqualTo("4b84793c-f3ea-4a55-92b7-942726aac163");
		assertThat(jwtClaimsSet.getStringClaim("space_guid")).isEqualTo("6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3");
		assertThat(jwtClaimsSet.getStringClaim("app_guid")).isEqualTo("02756191-d869-4806-9717-a6eec5142e8a");
	}

	@Test
	void testWithMissingCertificate() {
		RestClient restClient = this.restClientBuilder.apply(this.clientSsl.fromBundle("cacert"))
			.baseUrl("https://localhost:" + this.port)
			.build();
		ResponseEntity<String> response = restClient.post().uri("/token").retrieve().toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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

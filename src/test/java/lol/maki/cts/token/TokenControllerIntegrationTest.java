package lol.maki.cts.token;

import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"logging.level.org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider=debug",
		"logging.level.org.springframework.security.web.FilterChainProxy=debug",
		"jwt.audience=test"
})
public class TokenControllerIntegrationTest {

	@Autowired
	RestClient.Builder restClientBuilder;

	RestClient restClient;

	@LocalServerPort
	int port;

	@Autowired
	CertificatesInjectionFilter certificatesInjectionFilter;


	@BeforeEach
	void setUp() {
		this.restClient = restClientBuilder
				.baseUrl("http://localhost:" + port)
				.defaultStatusHandler(new DefaultResponseErrorHandler() {
					@Override
					protected void handleError(ClientHttpResponse response, HttpStatusCode statusCode) throws IOException {
						// NO-OP
					}
				})
				.build();
	}

	@Test
	void testWithValidCertificate() throws Exception {
		this.certificatesInjectionFilter.setInject(true);
		ResponseEntity<String> response = this.restClient.post()
				.uri("/token")
				.retrieve()
				.toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotEmpty();
		JWTClaimsSet jwtClaimsSet = JWTParser.parse(response.getBody()).getJWTClaimsSet();
		assertThat(jwtClaimsSet.getIssuer()).isEqualTo("http://localhost:" + port);
		assertThat(jwtClaimsSet.getAudience()).containsExactly("test");
		assertThat(jwtClaimsSet.getSubject()).isEqualTo("4b84793c-f3ea-4a55-92b7-942726aac163:6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3:02756191-d869-4806-9717-a6eec5142e8a");
		assertThat(jwtClaimsSet.getStringClaim("org_guid")).isEqualTo("4b84793c-f3ea-4a55-92b7-942726aac163");
		assertThat(jwtClaimsSet.getStringClaim("space_guid")).isEqualTo("6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3");
		assertThat(jwtClaimsSet.getStringClaim("app_guid")).isEqualTo("02756191-d869-4806-9717-a6eec5142e8a");
	}

	@Test
	void testWithMissingCertificate() {
		this.certificatesInjectionFilter.setInject(false);
		ResponseEntity<String> response = this.restClient.post()
				.uri("/token")
				.retrieve()
				.toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@TestConfiguration
	static class Config {
		static X509Certificate loadCertificate() {
			// TODO update cert
			ClassPathResource resource = new ClassPathResource("instance.crt");
			try (var stream = resource.getInputStream()) {
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				return (X509Certificate) cf.generateCertificate(stream);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Bean
		public CertificatesInjectionFilter certificatesInjectionFilter() {
			return new CertificatesInjectionFilter(new X509Certificate[] {loadCertificate()});
		}


		@Bean
		public FilterRegistrationBean<CertificatesInjectionFilter> clientCertificateMapperFilterRegistrationBean(CertificatesInjectionFilter certificatesInjectionFilter) {
			FilterRegistrationBean<CertificatesInjectionFilter> result = new FilterRegistrationBean<>(certificatesInjectionFilter);
			result.setOrder(Ordered.HIGHEST_PRECEDENCE);
			return result;
		}

	}

}

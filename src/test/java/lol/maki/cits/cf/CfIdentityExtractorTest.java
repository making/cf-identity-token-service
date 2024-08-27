package lol.maki.cits.cf;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

class CfIdentityExtractorTest {

	public static X509Certificate loadCertificate(String location) {
		Resource resource = new ClassPathResource(location);
		try (var stream = resource.getInputStream()) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf.generateCertificate(stream);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "instance-identity/instance.crt", "self-signed/client.crt" })
	void extractPrincipal(String path) {
		CfIdentityExtractor extractor = new CfIdentityExtractor();
		Object principal = extractor.extractPrincipal(loadCertificate(path));
		assertThat(principal).isEqualTo(
				"4b84793c-f3ea-4a55-92b7-942726aac163:6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3:02756191-d869-4806-9717-a6eec5142e8a");
	}

}
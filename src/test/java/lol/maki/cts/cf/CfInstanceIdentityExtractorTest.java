package lol.maki.cts.cf;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class CfInstanceIdentityExtractorTest {

	static X509Certificate loadCertificate() {
		ClassPathResource resource = new ClassPathResource("instance.crt");
		try (var stream = resource.getInputStream()) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf.generateCertificate(stream);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void extractPrincipal() {
		CfInstanceIdentityExtractor extractor = new CfInstanceIdentityExtractor();
		Object principal = extractor.extractPrincipal(loadCertificate());
		assertThat(principal).isEqualTo("4b84793c-f3ea-4a55-92b7-942726aac163/6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3/02756191-d869-4806-9717-a6eec5142e8a");
	}
}
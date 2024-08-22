package lol.maki.cts.cf;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import lol.maki.cts.CertUtils;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class CfInstanceIdentityExtractorTest {

	@Test
	void extractPrincipal() {
		CfInstanceIdentityExtractor extractor = new CfInstanceIdentityExtractor();
		Object principal = extractor.extractPrincipal(CertUtils.loadCertificate("instance.crt"));
		assertThat(principal).isEqualTo("4b84793c-f3ea-4a55-92b7-942726aac163:6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3:02756191-d869-4806-9717-a6eec5142e8a");
	}
}
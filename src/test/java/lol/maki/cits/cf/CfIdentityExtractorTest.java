package lol.maki.cits.cf;

import lol.maki.cits.CertUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CfIdentityExtractorTest {

	@ParameterizedTest
	@ValueSource(strings = { "classpath:instance-identity/instance.crt", "classpath:self-signed/client.crt" })
	void extractPrincipal(String path) {
		CfIdentityExtractor extractor = new CfIdentityExtractor();
		Object principal = extractor.extractPrincipal(CertUtils.loadCertificate(path));
		assertThat(principal).isEqualTo(
				"4b84793c-f3ea-4a55-92b7-942726aac163:6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3:02756191-d869-4806-9717-a6eec5142e8a");
	}

}
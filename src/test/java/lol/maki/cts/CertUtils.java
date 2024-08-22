package lol.maki.cts;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.springframework.core.io.ClassPathResource;

public class CertUtils {
	public static X509Certificate loadCertificate(String path) {
		ClassPathResource resource = new ClassPathResource(path);
		try (var stream = resource.getInputStream()) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf.generateCertificate(stream);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

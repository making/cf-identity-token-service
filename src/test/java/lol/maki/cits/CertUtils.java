package lol.maki.cits;

import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

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

	public static PrivateKey loadPrivateKey(String path) {
		ClassPathResource resource = new ClassPathResource(path);
		try (var lines = Files.lines(resource.getFile().toPath())) {
			String key = lines.filter(line -> !line.startsWith("-----")).collect(Collectors.joining());
			byte[] keyBytes = Base64.getDecoder().decode(key);
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(spec);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static KeyStore createKeyStore(String privateKey, String certificate, String password) {
		return createKeyStore(loadPrivateKey(privateKey), loadCertificate(certificate), password);
	}

	public static KeyStore createEmptyKeyStore() {
		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(null, null);
			return keyStore;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static KeyStore createKeyStore(PrivateKey privateKey, X509Certificate certificate, String password) {
		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(null, null);
			KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(privateKey,
					new java.security.cert.Certificate[] { certificate });
			KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(password.toCharArray());
			keyStore.setEntry("key", privateKeyEntry, passwordProtection);
			return keyStore;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static KeyStore createTrustStore(String path) {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			trustStore.setCertificateEntry("ca-cert", loadCertificate(path));
			return trustStore;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

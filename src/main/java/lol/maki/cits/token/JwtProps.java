package lol.maki.cits.token;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.io.ApplicationResourceLoader;
import org.springframework.boot.ssl.pem.PemContent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

@ConfigurationProperties(prefix = "jwt")
public final class JwtProps {

	private final String keyId;

	private final RSAPublicKey publicKey;

	private final RSAPrivateKey privateKey;

	private final List<String> audience;

	// to support `base64:` prefix
	private static final ResourceLoader resourceLoader = new ApplicationResourceLoader();

	public JwtProps(String keyId, String publicKey, String privateKey, List<String> audience) {
		this.keyId = keyId;
		this.publicKey = resourceToPublicKey(resourceLoader.getResource(publicKey));
		this.privateKey = resourceToPrivateKey(resourceLoader.getResource(privateKey));
		this.audience = audience;
	}

	public String keyId() {
		return keyId;
	}

	public RSAPublicKey publicKey() {
		return publicKey;
	}

	public RSAPrivateKey privateKey() {
		return privateKey;
	}

	public List<String> audience() {
		return audience;
	}

	static RSAPublicKey resourceToPublicKey(Resource resource) {
		try (InputStream stream = resource.getInputStream()) {
			byte[] content = Base64.getDecoder()
				.decode(StreamUtils.copyToString(stream, StandardCharsets.UTF_8)
					.replace("-----BEGIN PUBLIC KEY-----", "")
					.replace("-----END PUBLIC KEY-----", "")
					.replace("\n", ""));
			X509EncodedKeySpec spec = new X509EncodedKeySpec(content);
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	static RSAPrivateKey resourceToPrivateKey(Resource resource) {
		try (InputStream stream = resource.getInputStream()) {
			return (RSAPrivateKey) PemContent.of(StreamUtils.copyToString(stream, StandardCharsets.UTF_8))
				.getPrivateKey();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
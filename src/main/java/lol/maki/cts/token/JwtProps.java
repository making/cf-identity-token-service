package lol.maki.cts.token;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProps(String keyId, RSAPublicKey publicKey, RSAPrivateKey privateKey, List<String> audience) {
}
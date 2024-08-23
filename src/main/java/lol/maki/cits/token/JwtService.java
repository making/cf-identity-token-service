package lol.maki.cits.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class JwtService {

	private final JwtProps jwtProps;

	private JWSSigner signer;

	private JWSVerifier verifier;

	public JwtService(JwtProps jwtProps) {
		this.jwtProps = jwtProps;
		this.signer = new RSASSASigner(jwtProps.privateKey());
		this.verifier = new RSASSAVerifier(jwtProps.publicKey());
	}

	public SignedJWT sign(JWTClaimsSet claimsSet) {
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(this.jwtProps.keyId())
			.type(JOSEObjectType.JWT)
			.build();
		SignedJWT signedJWT = new SignedJWT(header, claimsSet);
		try {
			signedJWT.sign(this.signer);
		}
		catch (JOSEException e) {
			throw new IllegalStateException(e);
		}
		return signedJWT;
	}

	@PostConstruct
	void validateKeys() throws Exception {
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("test").build();
		SignedJWT signedJWT = sign(claimsSet);
		if (!signedJWT.verify(this.verifier)) {
			throw new IllegalStateException("The pair of public key and private key is wrong.");
		}
	}

}

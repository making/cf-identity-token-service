package lol.maki.cts.token;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import lol.maki.cts.cf.CfApp;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class TokenController {

	private final JwtService jwtService;

	private final JwtProps jwtProps;

	private final Clock clock;

	public TokenController(JwtService jwtService, JwtProps jwtProps, Clock clock) {
		this.jwtService = jwtService;
		this.jwtProps = jwtProps;
		this.clock = clock;
	}

	@PostMapping(path = "/token")
	public String token(@AuthenticationPrincipal CfApp cfApp, UriComponentsBuilder builder) {
		String issuer = builder.replacePath("").build().toString();
		Instant issuedAt = Instant.now(this.clock);
		Instant expiresAt = issuedAt.plus(12, ChronoUnit.HOURS);
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().issuer(issuer)
			.expirationTime(Date.from(expiresAt))
			.subject(cfApp.getUsername())
			.issueTime(Date.from(issuedAt))
			.audience(this.jwtProps.audience())
			.claim("org_guid", cfApp.orgGuid())
			.claim("space_guid", cfApp.spaceGuid())
			.claim("app_guid", cfApp.appGuid())
			.build();
		return this.jwtService.sign(claimsSet).serialize();
	}

	// https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_create_oidc.html#manage-oidc-provider-prerequisites
	@GetMapping(path = "/.well-known/openid-configuration")
	public Map<String, Object> openIdConfiguration(UriComponentsBuilder builder) {
		return Map.of("issuer", builder.replacePath("").build().toString(), "jwks_uri",
				builder.replacePath("token_keys").build().toString(), "response_types_supported", List.of("id_token"),
				"id_token_signing_alg_values_supported", List.of("RS256"), "subject_types_supported",
				List.of("public"));
	}

	@GetMapping(path = "/token_keys")
	public Map<String, Object> tokenKeys() {
		RSAKey key = new RSAKey.Builder(this.jwtProps.publicKey()).keyID(this.jwtProps.keyId()).build();
		return new JWKSet(key).toJSONObject();
	}

}

package lol.maki.cts.cf;

import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.web.authentication.preauth.x509.X509PrincipalExtractor;

public class CfInstanceIdentityExtractor implements X509PrincipalExtractor {
	private static final Pattern appPattern = Pattern.compile("OU=app:([a-fA-F0-9\\-]+)");

	private static final Pattern spacePattern = Pattern.compile("OU=space:([a-fA-F0-9\\-]+)");

	private static final Pattern orgPattern = Pattern.compile("OU=organization:([a-fA-F0-9\\-]+)");

	@Override
	public Object extractPrincipal(X509Certificate clientCert) {
		String subject = clientCert.getSubjectX500Principal().getName();
		String appGuid = extractGuid(appPattern, subject);
		String spaceGuid = extractGuid(spacePattern, subject);
		String organizationGuid = extractGuid(orgPattern, subject);
		return String.join("/", organizationGuid, spaceGuid, appGuid);
	}

	private static String extractGuid(Pattern pattern, String subject) {
		Matcher matcher = pattern.matcher(subject);
		if (matcher.find()) {
			return matcher.group(1);
		}
		else {
			return "";
		}
	}
}

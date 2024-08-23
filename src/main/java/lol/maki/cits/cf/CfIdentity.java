package lol.maki.cits.cf;

import java.util.Collection;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import static lol.maki.cits.cf.CfIdentityExtractor.SEPARATOR;

public final class CfIdentity implements UserDetails {

	private final String subject;

	private final String orgGuid;

	private final String spaceGuid;

	private final String appGuid;

	public static CfIdentity of(String subject) {
		return new CfIdentity(subject);
	}

	private CfIdentity(String subject) {
		this.subject = subject;
		String[] split = subject.split(SEPARATOR, 3);
		this.orgGuid = split[0];
		this.spaceGuid = split[1];
		this.appGuid = split[2];
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.createAuthorityList("ROLE_APP");
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return subject;
	}

	public String orgGuid() {
		return orgGuid;
	}

	public String spaceGuid() {
		return spaceGuid;
	}

	public String appGuid() {
		return appGuid;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (CfIdentity) obj;
		return Objects.equals(this.subject, that.subject);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subject);
	}

	@Override
	public String toString() {
		return "CfApp{orgGuid='%s', spaceGuid='%s', appGuid='%s'}".formatted(orgGuid, spaceGuid, appGuid);
	}

}

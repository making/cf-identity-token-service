package lol.maki.cts.token;

import java.io.IOException;
import java.security.cert.X509Certificate;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class CertificatesInjectionFilter implements Filter {

	private boolean inject = true;

	private final X509Certificate[] certificates;

	public CertificatesInjectionFilter(X509Certificate[] certificates) {
		this.certificates = certificates;
	}

	public void setInject(boolean inject) {
		this.inject = inject;
	}

	/**
	 * @see org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter
	 */
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		if (this.inject) {
			servletRequest.setAttribute("jakarta.servlet.request.X509Certificate", this.certificates);
		}
		filterChain.doFilter(servletRequest, servletResponse);
	}
}

package lol.maki.cits;

import java.time.Clock;

import lol.maki.cits.cf.CfAppIdentity;
import lol.maki.cits.cf.CfInstanceIdentityExtractor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(authz -> authz.requestMatchers("/token").hasRole("APP").anyRequest().permitAll())
			.x509(s -> s.x509PrincipalExtractor(new CfInstanceIdentityExtractor()))
			.csrf(AbstractHttpConfigurer::disable)
			.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return CfAppIdentity::of;
	}

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

}

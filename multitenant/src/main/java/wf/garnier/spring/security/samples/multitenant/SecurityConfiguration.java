package wf.garnier.spring.security.samples.multitenant;

import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import wf.garnier.spring.security.samples.multitenant.authentication.User;

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.authorizeHttpRequests(authConfig -> {
			authConfig.requestMatchers("/").permitAll();
			authConfig.requestMatchers("/secured/**").access(new TenantIdAuthorizationManager());
			authConfig.anyRequest().authenticated();
		})
			.formLogin(Customizer.withDefaults())
			.httpBasic(Customizer.withDefaults())
			.logout(logout -> logout.logoutSuccessUrl("/"))
			.build();

	}

	private static class TenantIdAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

		AuthenticationTrustResolverImpl trustResolver = new AuthenticationTrustResolverImpl();

		@Override
		public AuthorizationDecision check(Supplier<Authentication> authSupplier, RequestAuthorizationContext ctx) {
			var auth = authSupplier.get();

			if (trustResolver.isAuthenticated(auth)) {
				// Auth is null, not authenticated or anonymous
				return new AuthorizationDecision(false);

			}
			if (!(auth.getPrincipal() instanceof User user)) {
				// This should never happen as we only have one authentication type
				return new AuthorizationDecision(false);
			}

			// Check the tenantId vs the subdomain
			var subdomain = ctx.getRequest().getServerName().split("\\.")[0];
			var userTenant = user.getTenantId();
			return new AuthorizationDecision(subdomain.equals(userTenant));

		}

	}

}

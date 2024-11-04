package wf.garnier.security.samples.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;
import static org.springframework.security.authorization.AuthorizationManagers.allOf;

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.authorizeHttpRequests(auth -> {
			//@formatter:off
			auth.requestMatchers("/", "favicon.ico", "error").permitAll();
			auth.requestMatchers("/company/{companyId}/admin")
				.access(
						allOf(
								isInCompany(),
								hasRole("admin")
						)
				);
			auth.requestMatchers("/company/{companyId}/**").access(isInCompany());
			auth.anyRequest().denyAll();
			//@formatter:on
		})
			.formLogin(Customizer.withDefaults())
			.httpBasic(Customizer.withDefaults())
			.logout(logout -> logout.logoutSuccessUrl("/"))
			.build();
	}

	// Here we path the path variable name into which to look for the company ;
	// but it could be hardcoded.
	private AuthorizationManager<RequestAuthorizationContext> isInCompany() {
		return (authentication, requestAuthorizationContext) -> {
			if (authentication == null || !(authentication.get().getPrincipal() instanceof CustomUser user)) {
				return new AuthorizationDecision(false);
			}

			var companyId = requestAuthorizationContext.getVariables().get("companyId");
			return new AuthorizationDecision(user.getCompany().id().equals(companyId));
		};
	}

	@Bean
	UserDetailsService userDetailsService(CompanyRepository repository) {
		//@formatter:off
		return new CustomUserDetailsManager(
				new CustomUser("alice", repository.findById("alpha"), "user", "admin"),
				new CustomUser("bob", repository.findById("alpha"), "user"),
				new CustomUser("carol", repository.findById("omega"), "user"),
				new CustomUser("dave", repository.findById("omega"), "user", "admin")
		);
		//@formatter:on
	}

}
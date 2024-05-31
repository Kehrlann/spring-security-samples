package wf.garnier.spring.security.samples.two_openid;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableConfigurationProperties
class SecurityConfiguration {

    private static final String GOOGLE_ISSUER = "http://localhost:1111"; // should be https://accounts.google.com/
    private static final String AZURE_ISSUER = "http://localhost:2222"; // should be https://login.microsoftonline.com/<YOUR-TENANT-ID>/v2.0

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(
                        authorize -> {
                            authorize.requestMatchers("/").permitAll();
                            authorize.requestMatchers("/google").access(tokenWasIssuedBy(GOOGLE_ISSUER));
                            authorize.requestMatchers("/azure").access(tokenWasIssuedBy(AZURE_ISSUER));
                            authorize.anyRequest().authenticated();
                        }
                )
                .oauth2Login(Customizer.withDefaults())
                // Allow to logout by calling GET /logout
                .logout(l -> l.logoutRequestMatcher(new AntPathRequestMatcher("/logout", HttpMethod.GET.name())))
                .build();
    }

    /**
     * Check which identity provider created the authentication objet. It extracts the {@code issuer} that comes in the
     * ID token, which represents the Identity Provider that issued the token. It then compares it to the
     * {@code issuerUri} param.
     * <p>
     * Returns "false" for un-authenticated users.
     *
     * @param issuerUri -
     * @return -
     */
    private static AuthorizationManager<RequestAuthorizationContext> tokenWasIssuedBy(String issuerUri) {
        return (authSupplier, context) -> {
            if (authSupplier.get() instanceof OAuth2AuthenticationToken auth) {
                var issuer = ((OidcUser) auth.getPrincipal()).getIssuer().toString();
                return new AuthorizationDecision(issuer.equals(issuerUri));
            }
            return new AuthorizationDecision(false);
        };
    }
}

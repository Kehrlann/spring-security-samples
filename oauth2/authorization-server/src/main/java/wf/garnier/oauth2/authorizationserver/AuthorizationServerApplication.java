package wf.garnier.oauth2.authorizationserver;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@SpringBootApplication
public class AuthorizationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorizationServerApplication.class, args);
    }

    @Bean
    UserDetailsService userDetailsService() {
        var b = User.withDefaultPasswordEncoder();
        return new InMemoryUserDetailsManager(
                b.username("okta@garnier.wf").password("password").roles("user", "admin").build(),
                b.username("admin@example.org").password("password").roles("user").build(),
                b.username("user").password("password").roles("user").build()
        );
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> customizer() {

        return context -> {
            if (!context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
                return;
            }
            if (context.getPrincipal().getName().equals("admin@example.org")) {
                context.getClaims().claims(scopes -> {
                    Collection<String> scope = (Collection<String>) scopes.get("scope");
                    var x = new HashSet<>(scope);
                    x.add("conference.admin");
                    scopes.put("scope", x);
                });
            }
        };
    }

}

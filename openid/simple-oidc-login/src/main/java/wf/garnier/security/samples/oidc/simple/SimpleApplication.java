package wf.garnier.security.samples.oidc.simple;

import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SimpleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleApplication.class, args);
    }

    @Configuration
    @EnableWebSecurity
    static class SecurityConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .oauth2Login(Customizer.withDefaults())
                    .build();
        }
    }

    @RestController
    static class DemoController {
        @GetMapping("/")
        public String index(HttpServletRequest request, @AuthenticationPrincipal OidcUser user) {
            CsrfToken csrf = (CsrfToken) request.getAttribute("_csrf");
            var claims = user.getClaims()
                    .entrySet()
                    .stream()
                    .map(entry -> "<li><b>%s</b>: %s</li>".formatted(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"));
            return """
                    <h1>Logged in!</h1>
                    <p><b>raw id_token</b>: %s. All claims:</p>
                    <ul>
                    %s
                    </ul>
                    <form action="/logout" method="POST">
                        <input type="hidden" name="%s" value="%s" />
                        <button type="submit">Logout</button>
                    </form>
                    """
                    .formatted(user.getIdToken().getTokenValue(), claims, csrf.getParameterName(), csrf.getToken());
        }

    }

}

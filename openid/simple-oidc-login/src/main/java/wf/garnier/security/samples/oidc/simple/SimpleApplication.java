package wf.garnier.security.samples.oidc.simple;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
        public String index(HttpServletRequest request) {
            CsrfToken csrf = (CsrfToken) request.getAttribute("_csrf");
            return """
                    <h1>Logged in!</h1>
                    <form action="/logout" method="POST">
                        <input type="hidden" name="%s" value="%s" />
                        <button type="submit">Logout</button>
                    </form>
                    """
                    .formatted(csrf.getParameterName(), csrf.getToken());
        }

    }

}

package wf.garnier.security.samples.ott.servlet;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class OttApplication {

    public static void main(String[] args) {
        SpringApplication.run(OttApplication.class, args);
    }

    @Configuration
    static class SecurityConfiguration {

        OneTimeTokenGenerationSuccessHandler ottHandler = new RedirectOneTimeTokenGenerationSuccessHandler("/login/ott");

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .formLogin(Customizer.withDefaults())
                    .oneTimeTokenLogin(ott -> {
                        ott.tokenGenerationSuccessHandler((req, res, token) -> {
                            var value = token.getTokenValue();
                            System.out.println("Token value: " + value);
                            System.out.println("Link: http://localhost:8080/login/ott?token=" + value);
                            ottHandler.handle(req, res, token);
                        });
                    })
                    .build();
        }
    }

    @RestController
    public static class DemController {

        @GetMapping("/")
        public String index(HttpServletRequest request) {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
            return """
                    <h1>Logged in!</h1>
                    <form action="/logout" method="POST">
                        <input type="hidden" name="%s" value="%s" />
                        <button type="submit">Log out</button>
                    </form>
                    """
                    .formatted(csrfToken.getParameterName(), csrfToken.getToken());
        }

    }


}

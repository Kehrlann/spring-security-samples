package wf.garnier.experiments.ott;

import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ott.ServerOneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.server.authentication.ott.ServerRedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@SpringBootApplication
public class OttApplication {

    public static void main(String[] args) {
        SpringApplication.run(OttApplication.class, args);
    }

    @Configuration
    @EnableWebFluxSecurity
    static class SecurityConfiguration {

        ServerOneTimeTokenGenerationSuccessHandler ottHandler = new ServerRedirectOneTimeTokenGenerationSuccessHandler("/login/ott");

        @Bean
        public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
            return http
                    .authorizeExchange(auth -> {
                        auth.pathMatchers("/favicon.ico", "/error").permitAll();
                        auth.anyExchange().authenticated();
                    })
                    .formLogin(Customizer.withDefaults())
                    .oneTimeTokenLogin(ott -> ott.tokenGenerationSuccessHandler(
                            (exchange, oneTimeToken) -> {
                                System.out.println("ott token generation success - " + oneTimeToken.getTokenValue());
                                System.out.println("http://localhost:8080/login/ott?token=" + oneTimeToken.getTokenValue());
                                return ottHandler.handle(exchange, oneTimeToken);
                            }))
                    .build();
        }
    }

    @RestController
    static class OttController {
        @GetMapping("/")
        public Mono<String> index(ServerWebExchange exchange) {
            Mono<CsrfToken> csrf = exchange.getAttribute(CsrfToken.class.getName());
            return csrf.map(c -> """
                    <h1>Logged in!</h1>
                    <form action="/logout" method="POST">
                        <input type="hidden" name="%s" value="%s" />
                        <button type="submit">Logout</button>
                    </form>
                    """
                    .formatted(c.getParameterName(), c.getToken()));
        }
    }

}

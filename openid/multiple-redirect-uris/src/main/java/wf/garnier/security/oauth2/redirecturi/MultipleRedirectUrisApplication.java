package wf.garnier.security.oauth2.redirecturi;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class MultipleRedirectUrisApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultipleRedirectUrisApplication.class, args);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/alpha/authorized").permitAll();
                    auth.requestMatchers("/beta/authorized").permitAll();
                    auth.requestMatchers("/alpha/oauth2/authorization/alpha").permitAll();
                    auth.requestMatchers("/beta/oauth2/authorization/beta").permitAll();
                    auth.requestMatchers("/favicon.ico").permitAll();
                    auth.requestMatchers("/error").permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin(Customizer.withDefaults())
                .oauth2Login(oauth2 -> {
                    oauth2.authorizationEndpoint(authorizationEndpointConfig ->  {
                       authorizationEndpointConfig.baseUri("/{registrationId}/oauth2/authorization");
                    });
                    oauth2.withObjectPostProcessor(
                            new ObjectPostProcessor<OAuth2LoginAuthenticationFilter>() {
                                @Override
                                public <O extends OAuth2LoginAuthenticationFilter> O postProcess(O filter) {
                                    filter.setRequiresAuthenticationRequestMatcher(
                                            new OrRequestMatcher(
                                                    new AntPathRequestMatcher("/alpha/authorized", HttpMethod.GET.name()),
                                                    new AntPathRequestMatcher("/beta/authorized", HttpMethod.GET.name())
                                            ));
                                    return filter;
                                }
                            }
                    );
                })
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(new CustomAuthEntryPoint());
                })
                .build();
    }

}


@RestController
class DemoController {
    @GetMapping("/")
    public String hello(@AuthenticationPrincipal OidcUser user, HttpServletRequest request) {
        return """
                <h1>Hello %s!</h1>
                <p>You are on URL=[%s], logged in with client [%s].<p>
                """.formatted(user.getEmail(), request.getRequestURL(), user.getAudience());
    }
}

class CustomAuthEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public CustomAuthEntryPoint() {
        super("/");
    }

    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        if (request.getRequestURI().startsWith("/alpha")) {
            return "/alpha/oauth2/authorization/alpha";
        } else if (request.getRequestURI().startsWith("/beta")) {
            return "/beta/oauth2/authorization/beta";
        }
        return super.determineUrlToUseForThisRequest(request, response, exception);
    }
}
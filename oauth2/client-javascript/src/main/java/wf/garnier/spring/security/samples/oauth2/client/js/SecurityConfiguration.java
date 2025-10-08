package wf.garnier.spring.security.samples.oauth2.client.js;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authz -> {
                    authz.requestMatchers("/").permitAll();
                    authz.anyRequest().authenticated();
                })
                .oauth2Login(login -> login.successHandler(new SimpleUrlAuthenticationSuccessHandler("/")))
                .exceptionHandling(exc -> {
                    exc.authenticationEntryPoint(
                            (request, response, accessDeniedException) -> {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setHeader("x-redirect", "http://localhost:8084/oauth2/authorization/spring-auth-server");
                                response.getWriter().close();
                            }
                    );
                })
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .csrf(CsrfConfigurer::disable) // :scream:
                .build();
    }
    
}

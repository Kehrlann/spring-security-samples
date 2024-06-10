package wf.garnier.oauth2.client;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/").permitAll();
                    auth.requestMatchers("/css/**").permitAll();
                    auth.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
                    auth.requestMatchers("/admin").hasRole("admin");
                    auth.anyRequest().authenticated();
                })
                .formLogin(login -> login.defaultSuccessUrl("/private"))
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        var b = User.withDefaultPasswordEncoder();
        return new InMemoryUserDetailsManager(
                b.username("daniel").password("password").roles("user", "admin").build(),
                b.username("alice").password("password").roles("user").build()
        );
    }

}

package wf.garnier.oauth2.resourceserver;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ResourceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceServerApplication.class, args);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/conferences/admin").hasAuthority("SCOPE_conference.admin");
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(resource -> resource.jwt(Customizer.withDefaults()))
                .build();
    }


    record Conference(String name, String country, String city) {
    }

    private static final List<Conference> conferences = List.of(
            new Conference("Spring I/O", "Spain", "Barcelona"),
            new Conference("Voxxed Brussels", "Belgium", "Brussels"),
            new Conference("JFall", "Netherlands", "Ede"),
            new Conference("Devoxx France", "France", "Paris"),
            new Conference("Javaland", "Germany", "Nürburg")
    );

    private static final Map<String, List<Conference>> conferenceRepo =
            Map.of(
                    "okta@garnier.wf",
                    List.of(
                            new Conference("Spring I/O", "Spain", "Barcelona"),
                            new Conference("JFall", "Netherlands", "Ede"),
                            new Conference("Devoxx France", "France", "Paris")
                    )
            );

    @GetMapping("/conferences")
    List<Conference> conferences(@AuthenticationPrincipal Jwt jwt) {
        var subject = jwt.getSubject();
        var conferences = conferenceRepo.get(subject);
        return conferences != null ? conferences : Collections.emptyList();
    }

    @GetMapping("/conferences/admin")
    List<Conference> admin() {
        return conferences;
    }
}

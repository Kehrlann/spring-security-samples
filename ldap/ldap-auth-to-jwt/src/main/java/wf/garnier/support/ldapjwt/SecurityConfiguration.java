package wf.garnier.support.ldapjwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    private final JwtEncoder jwtEncoder = new NimbusJwtEncoder(

            new ImmutableSecret<>("3BE9DAE0-BE13-49BB-8A57-7F18398C1DA53BE9DAE0-BE13-49BB-8A57-7F18398C1DA5".getBytes())
    );

    // This works for ImmutableSecret jwt encoder
    private final MacAlgorithm macAlgorithm = MacAlgorithm.HS512;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(req -> {
                    req.anyRequest().authenticated();
                })
                .formLogin(form -> {
                    form.successHandler((request, response, authentication) -> {
                        response.setStatus(HttpStatus.OK.value());
                        Instant now = Instant.now();
                        JwtClaimsSet claims = JwtClaimsSet.builder()
                                .issuer("self")
                                .issuedAt(now)
                                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                                .subject(authentication.getName())
                                .claim("email", authentication.getName() + "@example.org")
                                .build();
                        var encoderParameters = JwtEncoderParameters.from(
                                JwsHeader.with(macAlgorithm).build(), claims);
                        var token = jwtEncoder.encode(encoderParameters).getTokenValue();
                        response.getWriter().write(token);
                        response.getWriter().close();
                    });
                })
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager() {
        var contextSource = new DefaultSpringSecurityContextSource("ldap://localhost:1389");
        contextSource.setUserDn("cn=admin,dc=example,dc=org");
        contextSource.setPassword("password");
        contextSource.afterPropertiesSet();
        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserDnPatterns("cn={0},ou=Users,dc=example,dc=org");
        factory.setUserSearchBase("ou=Users,dc=example,dc=org");
        factory.setUserSearchFilter("cn={0}");
        return factory.createAuthenticationManager();
    }
}

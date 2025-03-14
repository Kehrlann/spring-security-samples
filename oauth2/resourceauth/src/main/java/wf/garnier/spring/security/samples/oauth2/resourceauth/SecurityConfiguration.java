package wf.garnier.spring.security.samples.oauth2.resourceauth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    @Order(1)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //@formatter:off
		return http
				.securityMatcher("/conferences")
				.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.oauth2ResourceServer(resource -> {
					resource.jwt(Customizer.withDefaults());
				})
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.build();
		//@formatter:on
    }

    @Bean
    @Order(2)
    SecurityFilterChain authServerFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authServerConfig = OAuth2AuthorizationServerConfigurer
                .authorizationServer();

        //@formatter:off
		return http
				.authorizeHttpRequests(req -> req.anyRequest().authenticated())
				.with(authServerConfig, authserver ->{
                    authserver.oidc(Customizer.withDefaults());
                })
                .formLogin(Customizer.withDefaults())
			.build();
		//@formatter:on
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = loadRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        var rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("hardcoded").build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("oidc-client")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/spring-auth-server")
                .redirectUri("http://localhost:8080/login/oauth2/code/spring-auth-server")
                .scope("openid")
                .scope("email")
                .scope("profile")
                .scope("conference.list")
                .build();

        return new InMemoryRegisteredClientRepository(oidcClient);
    }

    @Bean
    UserDetailsService userDetailsService() {
        var b = User.withDefaultPasswordEncoder();
        return new InMemoryUserDetailsManager(
                b.username("okta@garnier.wf").password("password").roles("user", "admin").build(),
                b.username("admin@example.org").password("password").roles("user").build()
        );
    }

    private KeyPair loadRsaKey() {
        try {
            var factory = KeyFactory.getInstance("RSA");

            var privateKeyBytes = readKeyFromFile("keys/private.pem");
            var privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            var privateKey = factory.generatePrivate(privateKeySpec);

            var publicKeyBytes = readKeyFromFile("keys/public.pem");
            var publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            var publicKey = factory.generatePublic(publicKeySpec);
            return new KeyPair(publicKey, privateKey);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not load RSA key from file", ex);
        }
    }

    private static byte[] readKeyFromFile(String path) throws IOException {
        var privateKeyBytes = new ClassPathResource(path).getInputStream().readAllBytes();
        var privateKeyString = new String(privateKeyBytes).replace("\n", "");
        return Base64.getDecoder().decode(privateKeyString);
    }

}

package wf.garnier.oauth2.client.credentials;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class ClientCredentialsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientCredentialsApplication.class, args);
    }

    @Bean
    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
    InMemoryClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
        List<ClientRegistration> registrations = new ArrayList<>(
                new OAuth2ClientPropertiesMapper(properties).asClientRegistrations().values());
        return new InMemoryClientRegistrationRepository(registrations);
    }

    @Bean
    @ConditionalOnMissingBean
    OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    OAuth2AuthorizedClientRepository authorizedClientRepository(OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

    @Bean
    ApplicationRunner runner(
            RestClient.Builder clientBuilder,
            @Value("${conference.url}") String resourceServerUrl,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        OAuth2ClientCredentialsGrantRequest grantRequest = new OAuth2ClientCredentialsGrantRequest(
                clientRegistrationRepository.findByRegistrationId("spring-auth-server")
        );
        var accessToken = new RestClientClientCredentialsTokenResponseClient()
                .getTokenResponse(grantRequest)
                .getAccessToken();

        System.out.println("✅✅✅✅✅✅✅ Got access token: " + accessToken.getTokenValue());
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        var interceptor = new OAuth2ClientHttpRequestInterceptor(manager);
        interceptor.setClientRegistrationIdResolver(request -> "spring-auth-server");
        var client = clientBuilder
                .baseUrl(resourceServerUrl)
                .requestInterceptor(interceptor)
                .build();
        return args -> {
            var response = client.get()
                    .uri("/conferences")
                    .retrieve()
                    .body(String.class);
            System.out.println("✅✅✅✅✅✅✅");
            System.out.println(response);
        };
    }
}

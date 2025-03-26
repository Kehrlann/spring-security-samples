package wf.garnier.oauth2.client.credentials;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class ClientCredentialsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientCredentialsApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(
            RestClient.Builder clientBuilder,
            @Value("${conference.url}") String resourceServerUrl,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
OAuth2ClientCredentialsGrantRequest grantRequest = new OAuth2ClientCredentialsGrantRequest(
        clientRegistrationRepository.findByRegistrationId("spring")
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

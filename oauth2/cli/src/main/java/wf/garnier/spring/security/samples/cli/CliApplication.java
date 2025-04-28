package wf.garnier.spring.security.samples.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

// TODO: Need to exclude reactive oauth2 client autoconfig. Issue with Spring shell?
@SpringBootApplication(exclude = {ReactiveOAuth2ClientAutoConfiguration.class})
@CommandScan
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class CliApplication {

    public static void main(String[] args) {
        SpringApplication.run(CliApplication.class, args);
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

    @Command(group = "Request tokens", command = "token")
    @Component
    public static class TokenCommands {
        private final InMemoryClientRegistrationRepository clientRegistrationRepository;
        private final DeviceCodeGranter deviceCodeGranter;

        public TokenCommands(InMemoryClientRegistrationRepository clientRegistrationRepository, DeviceCodeGranter deviceCodeGranter) {
            this.clientRegistrationRepository = clientRegistrationRepository;
            this.deviceCodeGranter = deviceCodeGranter;
        }

        @Command(command = "client", description = "Request a token using the client_credentials grant")
        public String client(
                @Option(
                        longNames = {"scope", "scopes"},
                        shortNames = {'s'},
                        label = "The scopes to request. ",
                        arity = CommandRegistration.OptionArity.ZERO_OR_MORE,
                        description = "The scopes to request"
                ) List<String> scopes) {
            OAuth2ClientCredentialsGrantRequest grantRequest = new OAuth2ClientCredentialsGrantRequest(
                    clientRegistrationRepository.findByRegistrationId("spring-auth-server-client-credentials")
            );
            // TODO: scopes?

            var accessToken = new RestClientClientCredentialsTokenResponseClient()
                    .getTokenResponse(grantRequest)
                    .getAccessToken();
            return "Got token: " + accessToken.getTokenValue();
        }

        @Command(command = "device", description = "Request a token using the device code grant")
        public String deviceCode(
                @Option(
                        longNames = {"scope", "scopes"},
                        shortNames = {'s'},
                        label = "The scopes to request. ",
                        arity = CommandRegistration.OptionArity.ZERO_OR_MORE,
                        description = "The scopes to request"
                ) List<String> scopes) throws ExecutionException, InterruptedException {
            var deviceAuthRequest = deviceCodeGranter.authorize();
            System.out.println("🔗 Authenticate at: " + deviceAuthRequest.verificationUri());

            return deviceAuthRequest.accessToken().get();
        }
    }

}

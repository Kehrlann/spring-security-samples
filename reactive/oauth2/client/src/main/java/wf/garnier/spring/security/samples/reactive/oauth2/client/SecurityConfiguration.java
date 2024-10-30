package wf.garnier.spring.security.samples.reactive.oauth2.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration {

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.authorizeExchange(auth -> auth.anyExchange().authenticated())
			.oauth2Login(Customizer.withDefaults())
			.build();
	}

	@Bean
	public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
			ReactiveClientRegistrationRepository clientRegistrationRepository,
			ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {

		ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder
			.builder()
			.authorizationCode()
			.build();

		DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager = new DefaultReactiveOAuth2AuthorizedClientManager(
				clientRegistrationRepository, authorizedClientRepository);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

		return authorizedClientManager;
	}

	@Bean
	WebClient oauth2WebClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
				authorizedClientManager);
		return WebClient.builder().filter(oauth2Client).build();
	}

	@Bean
	WebClient customHeaderWebClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
				authorizedClientManager);
		return WebClient.builder().filter(oauth2Client).filter(new CustomAuthorizationExchangeFilterFunction()).build();
	}

}

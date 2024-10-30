package wf.garnier.spring.security.samples.reactive.oauth2.client;

import java.util.stream.Collectors;

import reactor.core.publisher.Mono;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@RestController
class DemoController {

	private final WebClient webClient;

	public DemoController(WebClient webClient) {
		this.webClient = webClient;
	}

	@GetMapping("/")
	public Mono<String> hello(@AuthenticationPrincipal OidcUser user) {
		return Mono.just("Hello " + user.getEmail());
	}

	@GetMapping("/messages")
	public Mono<String> messages(@RegisteredOAuth2AuthorizedClient("tanzu-local-authorization-server") OAuth2AuthorizedClient authorizedClient) {
		return webClient.get()
			.uri("http://localhost:8081/")
			.attributes(oauth2AuthorizedClient(authorizedClient))
			.retrieve()
			.bodyToFlux(Message.class)
			.map(Message::message)
			.collect(Collectors.joining(","));
	}

	record Message(String message) {

	}

}

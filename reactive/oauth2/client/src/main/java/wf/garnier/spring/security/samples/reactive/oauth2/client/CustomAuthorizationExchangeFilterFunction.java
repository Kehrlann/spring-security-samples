package wf.garnier.spring.security.samples.reactive.oauth2.client;

import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

class CustomAuthorizationExchangeFilterFunction implements ExchangeFilterFunction {

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return Mono.just(request).map(req -> {
			// Grab the original authorization header created by Spring Security
			var originalHeader = req.headers().getFirst("Authorization");
			// Extract the token from the header
			var token = originalHeader.toLowerCase().substring("bearer ".length());
			return ClientRequest.from(req)
				// Remove the existing header entirely ; because calling ".header" would add an additional
				// header value, rather than replace the existing value
				.headers(h -> h.remove("Authorization"))
				// Set the Authorization header to the desired value
				.header("Authorization", token)
				.build();
		}).flatMap(next::exchange);
	}

}

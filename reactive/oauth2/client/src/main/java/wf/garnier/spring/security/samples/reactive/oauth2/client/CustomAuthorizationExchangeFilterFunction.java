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
			var originalHeader = req.headers().getFirst("Authorization");
			var token = originalHeader.substring("bearer ".length());
			return ClientRequest.from(req)
				.header("X-Authorization", token)
				.headers(h -> h.remove("Authorization"))
				.build();
		}).flatMap(next::exchange);
	}

}

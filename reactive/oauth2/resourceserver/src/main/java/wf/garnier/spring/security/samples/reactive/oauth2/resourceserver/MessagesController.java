package wf.garnier.spring.security.samples.reactive.oauth2.resourceserver;

import java.util.stream.Stream;

import reactor.core.publisher.Flux;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class MessagesController {

    @GetMapping("/")
    public Flux<Message> messages(ServerHttpRequest request) {
        System.out.println("👀 Authorization: " + request.getHeaders().get("Authorization"));
        return Flux.fromStream(
                Stream.of(
                        new Message("hello"),
                        new Message("world")
                )
        );
    }

    @GetMapping("/public")
    public Flux<Message> publicMessages(ServerHttpRequest request) {
        System.out.println("👀 Authorization: " + request.getHeaders().get("Authorization"));
        return Flux.fromStream(
                Stream.of(
                        new Message("hello"),
                        new Message("world")
                )
        );
    }

    record Message(String message) {

    }
}

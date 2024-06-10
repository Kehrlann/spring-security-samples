package wf.garnier.oauth2.client;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

@Controller
class GreetingsController {

    private final RestClient restClient;

    public GreetingsController(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @GetMapping("/")
    public String publicPage() {
        return "public";
    }

    @GetMapping("/private")
    public String privatePage(Model model, Authentication authentication) {
        model.addAttribute("name", getName(authentication));
        return "private";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Conference(String name, String city) {
    }


    @GetMapping("/conferences")
    @ResponseBody
    public String conferences(@RegisteredOAuth2AuthorizedClient("spring-auth-server") OAuth2AuthorizedClient authorizedClient) {
        var token = authorizedClient.getAccessToken();

        var conferences = this.restClient.get()
                .uri("http://localhost:8081/conferences")
                .header("Authorization", "Bearer " + token.getTokenValue())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Conference>>() {
                })
                .stream()
                .map(c -> "<li>%s</li>".formatted(c))
                .collect(Collectors.joining("\n"));

        return """
                <h1>Conferences</h1>
                <ul>
                %s
                </ul>
                """.formatted(conferences);
    }

    @GetMapping("/conferences/admin")
    @ResponseBody
    public String conferencesAdmin(@RegisteredOAuth2AuthorizedClient("spring-auth-server") OAuth2AuthorizedClient authorizedClient) {
        var token = authorizedClient.getAccessToken();

        var conferences = this.restClient.get()
                .uri("http://localhost:8081/conferences/admin")
                .header("Authorization", "Bearer " + token.getTokenValue())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Conference>>() {
                })
                .stream()
                .map(c -> "<li>%s</li>".formatted(c))
                .collect(Collectors.joining("\n"));

        return """
                <h1>Conferences</h1>
                <ul>
                %s
                </ul>
                """.formatted(conferences);
    }

    private static String getName(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OidcUser user) {
            return user.getEmail();
        }
        return authentication.getName();
    }

    @GetMapping("/admin")
    @ResponseBody
    public String adminPage() {
        return """
                <h1>Admin Page</h1>
                <a href="/private">Go to private page</a>
                """;
    }
}

package wf.garnier.spring.security.samples.two_openid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class GreetingsController {

    @GetMapping("/")
    public String publicPage() {
        return """
                Hi! Please choose a page:
                <br>
                <br>
                <a href="/google">Google (must be authenticated with Google)</a>
                <br>
                <a href="/azure">Azure (must be authenticated with Azure)</a>
                """;
    }

    @GetMapping("/google")
    public String googlePage(@AuthenticationPrincipal OidcUser user) {
        return """
                Hello %s, this is the Google page.
                <br>
                <a href="/">Back to the main page</a>
                <a href="/logout">Logout</a>
                """
                .formatted(user.getEmail());
    }

    @GetMapping("/azure")
    public String azurePage(@AuthenticationPrincipal OidcUser user) {
        return """
                Hello %s, this is the Azure page.
                <br>
                <a href="/">Back to the main page</a>
                <a href="/logout">Logout</a>
                """
                .formatted(user.getEmail());
    }
}

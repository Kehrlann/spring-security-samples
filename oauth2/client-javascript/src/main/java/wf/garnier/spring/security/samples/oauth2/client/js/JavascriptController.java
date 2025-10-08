package wf.garnier.spring.security.samples.oauth2.client.js;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class JavascriptController {

    @GetMapping("/")
    public String publicPage() {
        return """
                 <html>
                <script>
                    document.addEventListener("DOMContentLoaded", () => {
                        document.getElementById("whoami")
                            .addEventListener("click", async () => {
                                try {
                                    const r = await fetch("/me")
                                    if (r.status === 401) {
                                        window.location.href = r.headers.get("x-redirect");
                                        return;
                                    }
                                    const data = await r.json();
                                    document.getElementById("user").textContent = JSON.stringify(data)
                                } catch (e) {
                                    console.error("Woops")
                                    console.error(e)
                                }
                            });
                    })
                </script>
                <body>
                <h1>Hello</h1>
                <button id="whoami">Who am i?</button>
                <div id="user">
                </div>
                <h2>Logout?</h2>
                <form action="/logout" method="POST">
                <button type="submit">Log out</button>
                </form>
                </body>
                </html>
                """;
    }

    @GetMapping("/private")
    public String privatePage() {
        return "This is private";
    }

    @GetMapping("/me")
    public Person me(Authentication authentication) {
        return new Person(authentication.getName());
    }

    record Person(String name) {

    }
}



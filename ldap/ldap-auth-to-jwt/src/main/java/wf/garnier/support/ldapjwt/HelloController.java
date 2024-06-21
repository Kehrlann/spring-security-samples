package wf.garnier.support.ldapjwt;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

    @GetMapping("/hello")
    public String hello(Authentication authentication) {
        return "you are logged in as: " + authentication.getName();
    }
}

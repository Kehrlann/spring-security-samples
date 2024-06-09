package com.example.ssl_and_formlogin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hi";
    }

    @GetMapping("/public")
    public String publicHello() {
        return "This is public";
    }

    @GetMapping({"/x509", "/x509-again"})
    public String x509Hello() {
        return "Hello from x509";
    }

    @GetMapping({"/formlogin", "/formlogin-again"})
    public String formLogin() {
        return "Hello from formlogin";
    }
}

package wf.garnier.security.oauth2.subpath;


import java.io.IOException;

import jakarta.servlet.ServletException;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SubpathApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubpathApplication.class, args);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .build();
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatAccessLogToConsole() {
        return factory -> factory.addContextCustomizers(context -> {
            Valve valve = new ValveBase() {
                @Override
                public void invoke(Request request, Response response) throws IOException, ServletException {
                    System.out.printf("~~~~~~~~ remote addr: %s%n", request.getRemoteAddr());
                    request.getHeaderNames().asIterator().forEachRemaining(
                            name -> {
                                System.out.printf("    %s: %s%n", name, request.getHeader(name));
                            }
                    );
                    getNext().invoke(request, response);
                }
            };
            context.getPipeline().addValve(valve);
        });
    }

}


@RestController
class DemoController {
    @GetMapping("/")
    public String hello(Authentication auth) {
        return """
                <h1>Hello %s!</h1>
                """.formatted(auth.getName());
    }
}
package wf.garnier.spring.security.samples.formlogin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class FormloginApplication {

    public static void main(String[] args) {
        SpringApplication.run(FormloginApplication.class, args);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/favicon.ico").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/30**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin(Customizer.withDefaults())
                .csrf(CsrfConfigurer::disable)
        .requestCache(rc -> {
            var requestCache = new HttpSessionRequestCache() {
                @Override
                public HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response) {
                    return null;
                }
            };
            rc.requestCache(requestCache);
        })
                .build();
    }

    @Bean
    BeanPostProcessor beanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof FilterChainProxy fcp) {
                    fcp.setFirewall(new DefaultHttpFirewall());
                }
                return bean;
            }
        };
    }

    @RestController
    static class DemoController {

        @GetMapping("/{value}")
        String hello(@PathVariable String value) {
            return """
                    <h1>Hello %s</h1>
                    <form action="/30%%20%%25%%20off" method="POST">
                    <input name="value" id="value" value="some pre-filled value" />
                    <button type="submit">Submit</button>
                    </form>
                    """.formatted(value);
        }

        @PostMapping("/{path}")
        String post(@PathVariable String path, @RequestParam String value) {
            return "Path : [%s], POST value : [%s]".formatted(path, value);
        }
    }

}

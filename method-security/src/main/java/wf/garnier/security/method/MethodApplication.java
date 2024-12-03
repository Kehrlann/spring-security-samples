package wf.garnier.security.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.PrePostTemplateDefaults;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableMethodSecurity
public class MethodApplication {

    public static void main(String[] args) {
        SpringApplication.run(MethodApplication.class, args);
    }

    @Bean
    PrePostTemplateDefaults prePostTemplateDefaults() {
        return new PrePostTemplateDefaults();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.build();
    }

    @RestController
    static class DemoController {

        @GetMapping("/one")
        @CustomAuthOne(someString = "hello")
        public String one() {
            return "hello one";
        }

        @GetMapping("/two")
        @CustomAuthTwo(someStringArray = {"'one'", "'two'"})
        public String two() {
            return "hello two";
        }

        @GetMapping("/three")
        @CustomAuthThree(someStringArray = {"'one'", "'two'"})
        public String three() {
            return "hello three";
        }

        @GetMapping("/four")
        @CustomAuthFour(someStringArray = {"one", "two"})
        public String four() {
            return "hello four";
        }

        @GetMapping("/five")
        @CustomAuthFive(someString = "hello", someStringArray = {"'one'", "'two'"}, someBoolean = false)
        public String five() {
            return "hello five";
        }

        @GetMapping("/six")
        @CustomAuthSix(permissions = {Permissions.CREATE, Permissions.READ})
        public String six() {
            return "hello six";
        }
    }


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.one(authentication, '{someString}')")
    @interface CustomAuthOne {

        String someString();
    }


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.two(authentication, {someStringArray})")
    @interface CustomAuthTwo {

        String[] someStringArray();

    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.three({someStringArray})")
    @interface CustomAuthThree {

        String[] someStringArray();

    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.four('{someStringArray}')")
    @interface CustomAuthFour {

        String[] someStringArray();

    }


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.five(authentication, '{someString}', {someBoolean}, {someStringArray})")
    @interface CustomAuthFive {

        String someString();

        String[] someStringArray();

        boolean someBoolean();

    }


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.six('{permissions}')")
    @interface CustomAuthSix {

        Permissions[] permissions();

    }


    static enum Permissions {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }

    @Component(value = "authorizationService")
    static class AuthorizationService {

        public boolean one(Authentication authentication, String someString) {
            System.out.println("Authentication: " + authentication);
            System.out.println("SomeString: " + someString);
            return true;
        }

        public boolean two(Authentication authentication, String... someStringArray) {
            System.out.println("Authentication: " + authentication);
            System.out.println("SomeStringArray: " + Arrays.toString(someStringArray));
            return true;
        }

        public boolean three(String... someStringArray) {
            System.out.println("Authentication: " + SecurityContextHolder.getContext().getAuthentication());
            System.out.println("SomeStringArray: " + Arrays.toString(someStringArray));
            return true;
        }

        public boolean four(String[] someStringArray) {
            System.out.println("Authentication: " + SecurityContextHolder.getContext().getAuthentication());
            System.out.println("SomeStringArray: " + Arrays.toString(someStringArray));
            return true;
        }

        public boolean five(Authentication authentication, String someString, boolean someBoolean, String... someStringArray) {
            System.out.println("Authentication: " + authentication);
            System.out.println("SomeString: " + someString);
            System.out.println("SomeBoolean: " + someBoolean);
            System.out.println("SomeStringArray: " + Arrays.toString(someStringArray));
            return true;
        }

        public boolean six(Permissions[] permissions) {
            System.out.println("Permissions: " + Arrays.toString(permissions));
            return true;
        }
    }


}

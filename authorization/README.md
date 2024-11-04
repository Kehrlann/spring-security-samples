# Authorization

This sample showcases non-trivial authorization scenarios.

Run the project with JDK 21+:

```
./gradlew bootRun
```


## Description

In this app, users are part of a company, either `alpha` or `omega`. They also have roles, e.g.
`user` or `admin`.

Users can only see the pages of their companies within `/company/{companyId}/**`; and only admins can see the
`/company/{companyId}/admin` page.

Here are our users. They all share the same password `password`:

| username | company      | roles           |
|----------|--------------|-----------------|
| `alice`  | `Alpha Corp` | `user`, `admin` |
| `bob`    | `Alpha Corp` | `user`          |
| `carol`  | `Omega Inc`  | `user`          |
| `dave`   | `Omega Inc`  | `user`, `admin` |

So `alice` can visit http://localhost:8080/company/alpha/admin, but not http://localhost:8080/company/omega.
Similarly, `carol` can only visit http://localhost:8080/company/omega.

You can also test with:

```
curl http://localhost:8080/company/alpha/admin --user alice:password --fail
```

## Explanation

Our users have roles implemented as `GrantedAuthorities`, e.g. `ROLE_user`. However, the company they belong to is a
special property of the user, with a custom `Company` type.

Filtering by roles is done by the usual `.requestMatchers(".../admin").hasRole("admin")`, see reference
documentation [Servlet > Authorization > HTTP](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html#match-requests).

If we want to authorize based on the company, we can't rely on roles or authorities. Before Spring Security 6, we would
have needed a special bean to check for access and maybe a SpEL expression like
`.access("hasRole('admin') && @companyVerifier.isInCompany(authentication)")`. With newer versions, we can do this
programmatically, with the `AuthorizationManager<RequestAuthorizationContext>` type. It is a functional interface, that
takes the authentication and the "authentication context" (here, think "the request") as parameters, and returns an
`AuthorizationDecision` which can be true or false,
see [reference docs](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html#remote-authorization-manager).
We'd write something like:

```java
private static AuthorizationManager<RequestAuthorizationContext> isInCompany() {
    return (authentication, requestAuthorizationContext) -> {
        if (authentication == null) {
            return new AuthorizationDecision(false);
        }

        if (!(authentication.get().getPrincipal() instanceof CustomUser user)) {
            return new AuthorizationDecision(false);
        }

        var companyId = requestAuthorizationContext.getVariables().get("companyId");
        return new AuthorizationDecision(user.getCompany().id().equals(companyId));
    };
}
```

Which states:

1. The user MUST be authenticated
2. The user MUST be of type CustomUser (which should always be the case in our app)
3. The user's companyId MUST match that of the request

This can then be used:

```java

@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/company/{companyId}/**").access(isInCompany());
                auth.anyRequest().denyAll();
            })
            // ...
            .build();
}
```

If we want to combine this check and the admin role check, we can reuse the authorization manager above, that is
designed to do one thing, and compose it with other authorization rules. Here we'll use
`AuthorizationManagers.allOf(...)` and combine our authorization rule with `AuthorizationManagers.hasRole(...)`.

Note that the admin rule is more specific than the company "non-admin" endpoints, so it must come first:

```java

@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/company/{companyId}/admin")
                        .access(
                                allOf(
                                        isInCompany(),
                                        hasRole("admin")
                                )
                        );
                auth.requestMatchers("/company/{companyId}/**").access(isInCompany());
                auth.anyRequest().denyAll();
            })
            // ...
            .build();
}
```

And there you go, here's one way to create your own custom authorization rules.

# Multi-tenant application

This sample showcases a basic multi-tenant application, using Postgres as a backing database, with one
schema per tenant.

Each tenant is decided by the subdomain of the incoming request, either `red.` or `black.` . In order
to have those subdomains locally, we use [nip.io](https://nip.io/) to point to localhost. To access
the app, navigate to either:

- http://127.0.0.1.nip.io:8080 (no tenant defined)
- http://red.127.0.0.1.nip.io:8080 (red tenant)
- http://black.127.0.0.1.nip.io:8080 (black tenant)

On the red tenant, there's a `red-user`, and on the black tenant, a `black-user`. Both use the `password`
password.

In both tenants, there is a `/todo` endpoint, that has no specific security, and a `/secured/todo` that
has specific Spring-Security protections.

## Requirements

- Java 21+ (I guess 17 would be fine though)
- Docker (to run postgres)

## Basic setup

To enable multi-tenancy, we follow
this [blog post](https://spring.io/blog/2022/07/31/how-to-integrate-hibernates-multitenant-feature-with-spring-data-jpa-in-a-spring-boot-application)
from the Spring blog. This is beyond security, it shows how to connect to a specific database schema
depending on which tenant the current request is targetting.

The important work is done in `TenantIdentifierResolver`, which, for a given request, extracts the subdomain
and returns that as the tenant ID.

```java

@Override
public String resolveCurrentTenantIdentifier() {
    // Can't get @RequestScope to work, because at init time, this is called outside
    // of a request scope.
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .filter(ServletRequestAttributes.class::isInstance)
            .map(ServletRequestAttributes.class::cast)
            .map(ServletRequestAttributes::getRequest)
            .map(HttpServletRequest.class::cast)
            .map(HttpServletRequest::getServerName)
            .map(s -> s.split("\\.")[0])
            .orElse("red");
}
```

Our users are loaded from different schema. To know to which schema the user belongs, we use the postgres
keyword `CURRENT_SCHEMA` and load that into a property in our `User` model. This will allow us to make
security checks for logged-in users:

```java

@Entity(name = "app_user")
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private Long id;

    private String password;

    private String username;

    @Formula("CURRENT_SCHEMA")
    private String tenantId;

    // ...
}
```

## Security profile of the application

There are two ways to authenticate with the application:

- HTTP basic in your terminal
- Form-login with a session cookie in your browser

### Security through data separation

Basic security is in place just by having separate schemas, so that you cannot log in on `red.` with `black-user`.
The same would apply with separate databases. Let's verify this with basic cURL calls.

An anonymous call to `/todo` fails:

```
curl http://red.127.0.0.1.nip.io:8080/todo -I

# Response:
#
# HTTP/1.1 401
# ...
```

The `red-user` can talk to the `red.` tenant:

```
curl http://red.127.0.0.1.nip.io:8080/todo --user red-user:password

# Response:
#
# <h1>Todo list</h1>
# <p>Current domain: <strong>red.127.0.0.1.nip.io</strong></p>
# <p>Logged in as: <strong>red-user</strong></p>
# <ul>
#     <li>red one</li>
# <li>red two</li>
# </ul>
# <p><a href="/">Go to index</a></p>
```

However, the `red-user` cannot talk to the `black.` tenant, because, when we verify the user,
it targets the `black` schema, where there is no `red` user!

```
curl http://black.127.0.0.1.nip.io:8080/todo --user red-user:password -I

# Response:
#
# HTTP/1.1 401
# ...
```

### Security through cookie domains

If a `red-user` logs in, using their browser, into `http://red.127.0.0.1.nip.io/todo`, and then they try to
navigate to `http://black.127.0.0.1.nip.io/todo`, they will be presented with another login screen. This is
because the cookie holding their session, `JSESSIONID`, is set to be valid on the host on which they first
logged in (`red.`). So the browser does not send their session cookie when they visit another host (`black.`)

If you change the setting of your cookies to be valid on any subdomain of `127.0.0.1.nip.io`, by setting in
your properties:

```yaml
# You SHOULD NOT be doing this, unless you have a very good reason 
server:
  servlet:
    session:
      cookie:
        domain: 127.0.0.1.nip.io
```

Then the cookie is valid across domain, and `red-user` will be able to go to `black.127.0.0.1.nip.io`, woops.

### Security breach, or "The cookie crumbles"

However, this is _browser_ restriction, that is used to protect the end-user from malicious websites, and not
protect the server from malicious users!

So, if you grab the `red-user`'s JSESSIONID cookie, conveniently displayed on `http://red.127.0.0.1.nip.io/todo`,
and then use it with cURL:

```
curl  http://black.127.0.0.1.nip.io:8080/todo -H "Cookie: JSESSIONID=<your session id goes here>"        

# Response:
# 
# <h1>Todo list</h1>
# <p>Current domain: <strong>black.127.0.0.1.nip.io</strong></p>
# <p>Logged in as: <strong>red-user</strong></p>
# <p>Session ID: <strong>A2FDAF959EE28921DE9031086EA5D3D1</strong></p>
# <ul>
#     <li>black one</li>
# <li>black two</li>
# </ul>
# <p><a href="/">Go to index</a></p>
# <br><br>
# <p><em>PSA: don't display sensitive credentials, like the session ID, on your webpage.</em></p>
```

Users can "escape" their domain, by using a legitimate cookie. That's pretty bad.

## Securing the app

### Using an `AuthorizationManager`

We want to block `red-user` to access the `black.` tenant, and vice-versa. If our app does not have to handle too many
permissions per-tenant (e.g. admin role, user role, etc), the we can write an `AuthorizationManager` and use that:

```java
private static class TenantIdAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    AuthenticationTrustResolverImpl trustResolver = new AuthenticationTrustResolverImpl();

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authSupplier, RequestAuthorizationContext ctx) {
        var auth = authSupplier.get();

        if (!trustResolver.isAuthenticated(auth)) {
            // Auth is null, user is not authenticated, or user is anonymous
            // -> deny access
            return new AuthorizationDecision(false);

        }
        if (!(auth.getPrincipal() instanceof User user)) {
            // This should never happen as we only have one authentication type in our app
            // In any case, we should set a secure default
            // -> deny access
            return new AuthorizationDecision(false);
        }

        // Check the tenantId vs the subdomain, and authorize when they match
        var subdomain = ctx.getRequest().getServerName().split("\\.")[0];
        var userTenant = user.getTenantId();

        return new AuthorizationDecision(subdomain.equals(userTenant));

    }
}
```

This can be used in our Security configuration, just like an `.authenticated()` or `.hasRoles(...)` rule:

```java

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authConfig -> {
                    authConfig.requestMatchers("/").permitAll();
                    // The /secured/... endpoints will have tenant protection
                    authConfig.requestMatchers("/secured/**").access(new TenantIdAuthorizationManager());
                    authConfig.anyRequest().authenticated();
                })
                // ...
                .build();
    }
}
```

So if we try our call again, `red-user` on `black.` tenant, but targeting the `/secured/todo` endpoint instead of
`/todo`, then our custom authorization manager kicks in, and the request is rejected:

```
curl  http://black.127.0.0.1.nip.io:8080/secured/todo -H "Cookie: JSESSIONID=<your session id goes here>"

# Response:
# 
# {"timestamp":"2024-08-26T08:38:23.313+00:00","status":403,"error":"Forbidden","message":"Forbidden","path":"/secured/todo"}
```

This approach works, but in practice only to be used to secure "everything":

```java

@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(authConfig -> {
                authConfig.requestMatchers("/", "/public", "...").permitAll();
                authConfig.anyRequest().access(new TenantIdAuthorizationManager());
            })
            // ...
            .build();
}
```

And it does easily support e.g. checking for roles.

### Using a filter

If you want to retain the usual fine-grained authorization in addition to tenant isolation, you could consider using a
`Filter`, and apply it right before `AuthorizationFilter`, where authorizations (e.g, roles) are checked. This way,
you'll ensure an `Authentication` will be available in the `SecurityContext` when applicable. Something like:

```java
class TenantVerificationFilter extends OncePerRequestFilter {
    
	// Used to verify the user is logged in when making this request
	private final AuthenticationTrustResolverImpl trustResolver = new AuthenticationTrustResolverImpl();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		var authentication = SecurityContextHolder.getContext().getAuthentication();

		if (!trustResolver.isAuthenticated(authentication)) {
			// If the user is not logged in, we cannot know the tenant for which this
			// connection is authorized, as we have no user info, and no tenant ID.
			// Security is delegated to the rest of the filter chain, which will check
			// whether the user needs to be authenticated to access this page.
			// This is useful for anonymous access to a public page.
			filterChain.doFilter(request, response);
			return;
		}

		if (!(authentication.getPrincipal() instanceof User user)) {
			// Should never happen, we only have the `User` type for our users.
			throw new ServletException("This should never happen");
		}

		var subdomain = request.getServerName().split("\\.")[0];
		if (!subdomain.equals(user.getTenantId())) {
			var errorMessage = "Invalid tenant, trying to access [%s] but user [%s] is in tenant [%s]"
				.formatted(subdomain, user.getUsername(), user.getTenantId());
			System.out.println("~~~~~~~~> " + errorMessage);
            // From org.springframework.security.access, not java.nio.file !
            throw new AccessDeniedException(errorMessage);
		}
		else {
			var successMessage = "Valid tenant, trying to access [%s], user [%s] is in tenant [%s]".formatted(subdomain,
					user.getUsername(), user.getTenantId());
			System.out.println("~~~~~~~~> " + successMessage);
		}

		filterChain.doFilter(request, response);
	}

}
```

And update the security configuration:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(authConfig -> {
        authConfig.requestMatchers("/").permitAll();
        authConfig.anyRequest().authenticated();
    })
        .formLogin(Customizer.withDefaults())
        .httpBasic(Customizer.withDefaults())
        .logout(logout -> logout.logoutSuccessUrl("/"))
        .addFilterBefore(new TenantVerificationFilter(), AuthorizationFilter.class)
        .build();
}
```

And voilà! Cross-tenant requests are forbidden.
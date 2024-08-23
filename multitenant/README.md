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
keyword `CURRENT_SCHEMA` and load that into a property in `User`. This will allow us to make security checks
for logged-in users:

```java

@Formula("CURRENT_SCHEMA")
private String tenantId;
```

## Security

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

TODO

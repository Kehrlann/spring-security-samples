# Two OpenID providers

Spring Security with two OpenID providers.

This showcases logging in with two OIDC providers, one "Google" and one "Azure". Neither of those are "real" cloud
identity providers, rather we use two Docker containers, running the [Dex IDP](https://dexidp.io):

- "Google" runs on http://localhost:1111 ; log in with `user@google.com` + `password`
- "Azure" runs on http://localhost:2222 ; log in with `user@microsoft.com` + `password`

The app has 3 routes:
- `http://localhost:8080/`, which is public (no need to be authenticated)
- `http://localhost:8080/google`, which is private, AND you need to log in with "Google"
- `http://localhost:8080/azure`, which is private, AND you need to log in with "Azure"

All the "authorization" magic happens in `SecurityConfiguration`.
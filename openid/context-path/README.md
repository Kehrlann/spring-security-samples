# Context-path

This sample showcases how to deploy the same app behind different routes, and support OpenID connect
for both instances without code changes. It relies on the servlet context-path, configured through
the property `server.servlet.context-path` or `SERVER_SERVLET_CONTEXT_PATH`.

The app has a single OIDC client configured, and doesn't have oauth2/oidc specific configuration for
each context path.

The identity provider has a redirect URI for both apps.

The sample has a docker-compose file with:
1. The app, deployed twice, with context-path set to `/alpha` and `/beta`
    a. Note: the apps run with `./gradlew bootRun` so it takes a little while to start up those
    containers
2. The Dex OpenID Connect provider, which allows users to log in
    a. The username is `user@example.com` and the password is `password`
3. Nginx, which acts the "router" in front of all these apps. You can access the apps on
   http://localhost:8080/alpha and http://localhost:8080/beta


Run with:

```
docker compose up
```

# Context-path

Show multiple apps on different context paths, /alpha and /beta.

Uses the Dex identity provider, with user `user@example.com` / `password`.

Run with:

```
docker compose up
```


It will take a little while to start, as it needs to the Spring Boot app inside the docker
containers.


To use, access both routes:
- http://localhost:8080/alpha
- http://localhost:8080/beta

And login with `user@example.com` / `password`.

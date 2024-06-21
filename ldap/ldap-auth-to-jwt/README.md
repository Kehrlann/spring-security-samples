# spring-security-ldap-to-jwt


Basic demo that outputs a JWT when you log in with LDAP. You should probably not be doing this, but
this project is intended to solve someone's specific problem, and showcases the following flow:

1. User navigates to the login page (say "/login")
1. They enter their LDAP credentials and click Login
1. The credentials are sent via HTTP POST to the server
1. The server validates the credentials with an LDAP-bind authentication
1. If the credentials are valid, instead of issuing an HTTP redirect (the default for form-login),
   the server returns a 200 response, with a JWT as  the body.

Notes:

- The project uses an OpenLDAP docker container, with a custom LDIF
- It uses bind authentication with an admin account (admin/password)
- It does not populate authorities for the user logging in, it is left as a exercise for the reader.


Interesting docs:
- https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/ldap.html
- https://docs.spring.io/spring-ldap/reference/
- Maybe: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data.nosql.ldap

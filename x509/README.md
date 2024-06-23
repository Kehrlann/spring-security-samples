# SSL and FormLogin

This showcases logging in with either mTLS (client certificate), or with form login without a client certificate.

## Generate certificates

To make x509 work, you can follow
the [Baeldung tutorial](https://www.baeldung.com/x-509-authentication-in-spring-security) here.

All certificates and stores are generated with the `changeit` password/key.

```shell
# Make sure CN is "localhost"
openssl req -x509 -sha256 -days 3650 -newkey rsa:4096 -keyout rootCA.key -out rootCA.crt
openssl req -new -newkey rsa:4096 -keyout localhost.key -out localhost.csr

# Make it valid for 10 years
openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in localhost.csr -out localhost.crt -days 3650 -CAcreateserial -extfile localhost.ext
openssl x509 -in localhost.crt -text

# Generate keystore for serving https traffic
openssl pkcs12 -export -out localhost.p12 -name "localhost" -inkey localhost.key -in localhost.crt
keytool -importkeystore -srckeystore localhost.p12 -srcstoretype PKCS12 -destkeystore keystore.jks -deststoretype JKS

# Generate a truststore to trust the client certificate
keytool -import -trustcacerts -noprompt -alias ca -ext san=dns:localhost,ip:127.0.0.1 -file rootCA.crt -keystore truststore.jks

# Client cert
# Make sure CN is "Bob"
openssl req -new -newkey rsa:4096 -nodes -keyout clientBob.key -out clientBob.csr
openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in clientBob.csr -out clientBob.crt -days 365 -CAcreateserial
openssl pkcs12 -export -out clientBob.p12 -name "clientBob" -inkey clientBob.key -in clientBob.crt
```

## Use the app

NB: All certificates and stores were generated with `changeit` as the key or secret, and are
intended to work on `http://localhost:...`

Navigate to http://localhost:8080/ . You can log in with `bob` and pw: `password` ;
you should be able to navigate to http://localhost:8080/formlogin but NOT to
http://localhost:8080/x509 .

On the other hand, if you want to use mTLS you can make cURL requests to /x509 (/formlogin will
redirect you to /login):

```shell
curl --cacert localhost.crt --cert clientBob.p12:changeit --cert-type P12 https://localhost:8443/x509
```

Note:

- `--cacert` is so that curl trusts the Spring Boot App
- `--cert clientBob.p12:changeit --cert-type P12` is so that curl sends the client certificate when making requests

## Some notes on ordering

- HTTP basic takes precedence over other auth mechanisms
- x509 backs off by default when the request is already authenticated
  - Unless `AbstractPreAuthenticationFilter#setCheckForPrincipalChanges` is set to true
- So ordering is, by default: HTTP Basic, Session-based auth (e.g. form login), x509 as a last resort
- In the /basic endpoint, we showcase how to make a "backing-off" BasicAuthenticationFilter, which
  does not apply its auth logic if there already is an authentication in the SecurityContext

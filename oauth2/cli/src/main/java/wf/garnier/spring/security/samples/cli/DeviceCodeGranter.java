package wf.garnier.spring.security.samples.cli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class DeviceCodeGranter {

    private final ClientRegistration clientRegistration;
    private final RestClient restClient;

    public DeviceCodeGranter(InMemoryClientRegistrationRepository clientRegistrationRepository, RestClient.Builder restClientBuilder) {
        // TODO: public client support
        this.clientRegistration = clientRegistrationRepository.findByRegistrationId("spring-auth-server-device-code");
        this.restClient = restClientBuilder.build();
    }

    public DeviceCodeRequest authorize() {
        var body = new LinkedMultiValueMap<String, String>();
        body.add("client_id", clientRegistration.getClientId());
        body.add("scope", String.join(" ", clientRegistration.getScopes()));

        var deviceCodeRequest = this.restClient.post()
                .uri(clientRegistration.getProviderDetails().getConfigurationMetadata().get("device_authorization_endpoint").toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.setBasicAuth(clientRegistration.getClientId(), clientRegistration.getClientSecret()))
                .body(body)
                .retrieve()
                .body(OAuth2DeviceAuthorizationResponse.class);

        // TODO: cache result
        CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> {
            // TODO: support timeouts
            while (true) {
                try {
                    Thread.sleep(Duration.ofSeconds(5));
                    System.out.println("⏱️ Polling for token ...");
                    var tokenResponse = getToken(deviceCodeRequest.deviceCode());
                    System.out.println("✅ Success");
                    return tokenResponse.accessToken();
                } catch (HttpClientErrorException.BadRequest e) {
                    var errorResponse = e.getResponseBodyAs(OAuth2TokenErrorResponse.class);
                    if (errorResponse != null && errorResponse.isAuthorizationPending()) {
                        continue;
                    }
                    throw new RuntimeException("Error was: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("❌ Error");
                    throw new RuntimeException("Error was: " + e.getMessage());
                }
            }
        });

        return new DeviceCodeRequest(deviceCodeRequest.verificationUriComplete(), f);
    }

    private AccessTokenResponse getToken(String deviceCode) {
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", AuthorizationGrantType.DEVICE_CODE.getValue());
        body.add("device_code", deviceCode);
        body.add("client_id", clientRegistration.getClientId());

        return this.restClient.post()
                .uri(clientRegistration.getProviderDetails().getTokenUri())
                .headers(h -> h.setBasicAuth(clientRegistration.getClientId(), clientRegistration.getClientSecret()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(AccessTokenResponse.class);
    }


    public record DeviceCodeRequest(
            String verificationUri,
            CompletableFuture<String> accessToken
    ) {

    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record OAuth2DeviceAuthorizationResponse(
            String deviceCode,
            String userCode,
            String verificationUri,
            String verificationUriComplete,
            Instant expiresIn,
            int interval
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record OAuth2TokenErrorResponse(String error) {
        public boolean isAuthorizationPending() {
            return "authorization_pending".equals(error);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record AccessTokenResponse(String accessToken) {}

}

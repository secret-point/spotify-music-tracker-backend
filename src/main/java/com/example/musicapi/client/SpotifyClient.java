package com.example.musicapi.client;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class SpotifyClient {

    private final RestTemplate restTemplate;
    private final TokenHolder tokenHolder;

    @Value("${spotify.api.base-url}")
    private String baseUrl;

    @Value("${spotify.api.token-url}")
    private String tokenUrl;

    @Value("${spotify.api.client-id}")
    private String clientId;

    @Value("${spotify.api.client-secret}")
    private String clientSecret;

    @Value("${spotify.api.redirect-uri}")
    private String redirectUri;

    public void redirectToAuthorization(HttpServletResponse response) throws UnsupportedEncodingException {
        String scope = "user-read-private user-read-email";

        String authorizationUrl = UriComponentsBuilder.fromHttpUrl("https://accounts.spotify.com/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri, "UTF-8")
                .queryParam("scope", scope)
                .build()
                .toUriString();

        response.setHeader("Location", authorizationUrl);
        response.setStatus(HttpServletResponse.SC_FOUND);
    }

    public void getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        Map<String, Object> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
        ).getBody();

        if (response != null) {
            tokenHolder.setAccessToken((String) response.get("access_token"));
            tokenHolder.setRefreshToken((String) response.get("refresh_token"));
        }
    }

    public void refreshAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes()));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", tokenHolder.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        Map<String, Object> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
        ).getBody();

        if (response != null) {
            tokenHolder.setAccessToken((String) response.get("access_token"));
            if (response.containsKey("refresh_token")) {
                tokenHolder.setRefreshToken((String) response.get("refresh_token"));
            }
        }
    }


    public Map fetchTrackMetadata(String isrc) {
        if (tokenHolder.getAccessToken() == null) {
            throw new IllegalStateException("Access token is missing. Authenticate first.");
        }

        String url = String.format("%s/search?q=isrc:%s&type=track", baseUrl, isrc);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenHolder.getAccessToken());

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Map.class
        ).getBody();
    }

    public Map<String, Object> fetchAlbumDetails(String albumId) {
        String url = String.format("%s/albums/%s", baseUrl, albumId);

        return restTemplate.getForObject(
                url,
                Map.class,
                Map.of("Authorization", "Bearer " + tokenHolder.getAccessToken())
        );
    }

    private String generateRandomState() {
        return java.util.UUID.randomUUID().toString();
    }
}

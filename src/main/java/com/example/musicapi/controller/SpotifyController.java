package com.example.musicapi.controller;

import com.example.musicapi.client.SpotifyClient;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
public class SpotifyController {
    private final SpotifyClient spotifyClient;

    public SpotifyController(SpotifyClient spotifyClient) {
        this.spotifyClient = spotifyClient;
    }

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws UnsupportedEncodingException {
        spotifyClient.redirectToAuthorization(response);
    }

    @GetMapping("/callback")
    public String callback(@RequestParam String code) {
        spotifyClient.getAccessToken(code);
        return "Authorization successful!";
    }

    @GetMapping("/track")
    public Object getTrack(@RequestParam String isrc) {
        try {
            return spotifyClient.fetchTrackMetadata(isrc);
        } catch (Exception e) {
            spotifyClient.refreshAccessToken();
            return spotifyClient.fetchTrackMetadata(isrc);
        }
    }
}

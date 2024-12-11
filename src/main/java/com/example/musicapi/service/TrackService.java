package com.example.musicapi.service;

import com.example.musicapi.client.SpotifyClient;
import com.example.musicapi.model.Track;
import com.example.musicapi.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrackService {
    private final SpotifyClient spotifyClient;
    private final TrackRepository trackRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // Add RestTemplate

    public Track createTrack(String isrc) throws IOException {
        if (trackRepository.findByIsrc(isrc).isPresent()) {
            throw new IllegalStateException("Track already exists!");
        }

        // Fetch track metadata from Spotify API

        Map<String, Object> trackData = spotifyClient.fetchTrackMetadata(isrc);
        Map<String, Object> tracks = (Map<String, Object>) trackData.get("tracks");
        List<Map<String, Object>> items = (List<Map<String, Object>>) tracks.get("items");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("No track found for the provided ISRC.");
        }
        Map<String, Object> firstTrack = items.get(0);

        // Populate Track entity
        Track track = new Track();
        track.setIsrc(isrc);
        track.setName((String) firstTrack.get("name"));
        List<Map<String, Object>> artists = (List<Map<String, Object>>) firstTrack.get("artists");
        track.setArtistName((String) artists.get(0).get("name"));
        Map<String, Object> album = (Map<String, Object>) firstTrack.get("album");
        track.setAlbumName((String) album.get("name"));
        track.setAlbumId((String) album.get("id"));
        track.setIsExplicit((Boolean) firstTrack.get("explicit"));
        track.setPlaybackSeconds(((Integer) firstTrack.get("duration_ms")) / 1000);

        // Fetch album details to retrieve the cover image
        Map<String, Object> albumData = spotifyClient.fetchAlbumDetails(track.getAlbumId());
        List<Map<String, Object>> images = (List<Map<String, Object>>) albumData.get("images");
        String coverImageUrl = (String) images.get(0).get("url");

        // Download cover image and save to file
        byte[] coverImage = restTemplate.getForObject(coverImageUrl, byte[].class);
        Path imagePath = Path.of("covers/" + track.getAlbumId() + ".jpg");
        Files.createDirectories(imagePath.getParent());
        Files.write(imagePath, coverImage);

        // Set the cover image path
        track.setCoverImagePath(imagePath.toString());

        // Save track metadata to database
        return trackRepository.save(track);
    }

    public Track getTrackMetadata(String isrc) {
        return trackRepository.findByIsrc(isrc).orElseThrow(() -> new IllegalArgumentException("Track not found"));
    }

    public byte[] getCoverImage(String isrc) throws IOException {
        Track track = getTrackMetadata(isrc);
        return Files.readAllBytes(Path.of(track.getCoverImagePath()));
    }
}

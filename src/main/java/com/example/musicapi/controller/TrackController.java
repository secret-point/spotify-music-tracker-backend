package com.example.musicapi.controller;

import com.example.musicapi.model.Track;
import com.example.musicapi.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/codechallenge")
@RequiredArgsConstructor
public class TrackController {
    private final TrackService trackService;

    @PostMapping("/createTrack")
    public ResponseEntity<Track> createTrack(@RequestParam String isrc) throws IOException {
        return ResponseEntity.ok(trackService.createTrack(isrc));
    }

    @GetMapping("/getTrackMetadata")
    public ResponseEntity<Track> getTrackMetadata(@RequestParam String isrc) {
        return ResponseEntity.ok(trackService.getTrackMetadata(isrc));
    }

    @GetMapping("/getCover")
    public ResponseEntity<byte[]> getCover(@RequestParam String isrc) throws IOException {
        byte[] coverImage = trackService.getCoverImage(isrc);
        return ResponseEntity.ok().header("Content-Type", "image/jpeg").body(coverImage);
    }
}


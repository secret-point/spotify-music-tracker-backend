package com.example.musicapi.repository;

import com.example.musicapi.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {
    Optional<Track> findByIsrc(String isrc);
}

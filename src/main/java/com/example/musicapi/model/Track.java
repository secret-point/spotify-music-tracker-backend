package com.example.musicapi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String isrc;
    private String name;
    private String artistName;
    private String albumName;
    private String albumId;
    private Boolean isExplicit;
    private Integer playbackSeconds;
    private String coverImagePath;
}

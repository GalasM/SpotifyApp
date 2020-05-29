package com.example.spotifyapp.models;

public class Track {
    private String artist;
    private String name;
    private String uri;

    public Track(String artist, String name, String uri) {
        this.artist = artist;
        this.name = name;
        this.uri = uri;
    }

    public Track() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {
        return "Track{" +
                "artist='" + artist + '\'' +
                ", name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}

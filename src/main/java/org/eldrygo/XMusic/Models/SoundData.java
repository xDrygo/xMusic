package org.eldrygo.XMusic.Models;

public record SoundData(String sound, float volume, float pitch, String name, String author, int durationSeconds) {

    @Override
    public String sound() {
        return sound;
    }

    @Override
    public float volume() {
        return volume;
    }

    @Override
    public float pitch() {
        return pitch;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String author() {
        return author;
    }

    @Override
    public int durationSeconds() {
        return durationSeconds;
    }
}
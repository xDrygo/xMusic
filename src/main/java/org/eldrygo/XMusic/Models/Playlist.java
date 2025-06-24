package org.eldrygo.XMusic.Models;

import java.util.List;

public record Playlist(List<SoundData> sounds, boolean shuffle) {
    @Override
    public List<SoundData> sounds() {
        return sounds;
    }

    @Override
    public boolean shuffle() {
        return shuffle;
    }
}
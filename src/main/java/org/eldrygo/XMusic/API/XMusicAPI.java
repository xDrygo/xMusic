package org.eldrygo.XMusic.API;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.eldrygo.XMusic.Managers.MusicManager;
import org.eldrygo.XMusic.Models.Playlist;
import org.eldrygo.XMusic.Models.SoundData;

import java.util.Collection;
import java.util.Set;

public class XMusicAPI {

    private static MusicManager musicManager;

    public static void init(MusicManager manager) {
        musicManager = manager;
    }

    public static void playSong(Player player, String key, SoundCategory category) {
        if (category == null) {
            musicManager.play(player, key);
        } else {
            musicManager.play(player, key, category);
        }
    }

    public static void stopSong(Player player, String key) {
        musicManager.stop(player, key);
    }

    public static void stopCurrentSong(Player player) {
        musicManager.stopCurrentSong(player);
    }

    public static void playPlaylist(Collection<? extends Player> players, String playlistKey, SoundCategory category) {
        if (category == null) {
            musicManager.playPlaylist(players, playlistKey);
        } else {
            musicManager.playPlaylist(players, playlistKey, category);
        }
    }

    public static void stopPlaylist(Player player) {
        musicManager.stopPlaylist(player);
    }

    public static void stopPlaylistAndFinish(Player player) {
        musicManager.stopPlaylistFinish(player);
    }

    public static boolean skipSong(Player player) {
        return musicManager.skipSong(player);
    }

    public static SoundData getSoundData(String key) {
        return musicManager.getSoundData(key);
    }

    public static Playlist getPlaylist(String key) {
        return musicManager.getPlaylist(key);
    }

    public static Set<String> getAllSoundKeys() {
        return musicManager.getSoundKeys();
    }

    public static Set<String> getAllPlaylistKeys() {
        return musicManager.getPlaylistKeys();
    }

    public static void reloadMusicManager() {
        musicManager.reload();
    }
}

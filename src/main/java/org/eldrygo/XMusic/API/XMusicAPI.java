package org.eldrygo.XMusic.API;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.eldrygo.XMusic.Managers.MusicManager;
import org.eldrygo.XMusic.Managers.MusicManager.SoundData;
import org.eldrygo.XMusic.Managers.MusicManager.Playlist;

import java.util.Set;

public class XMusicAPI {

    private static MusicManager musicManager;

    // Llamado por el plugin principal en su onEnable
    public static void init(MusicManager manager) {
        musicManager = manager;
    }

    public static void playSongWithPluginSoundCategory(Player player, String key) {
        musicManager.play(player, key);
    }

    public static void playSong(Player player, String key, SoundCategory category) {
        musicManager.play(player, key, category);
    }

    public static void stopSong(Player player, String key) {
        musicManager.stop(player, key);
    }

    public static void stopCurrentSong(Player player) {
        musicManager.stopCurrentSong(player);
    }

    public static void playPlaylist(Player player, String playlistKey) {
        musicManager.playPlaylist(player, playlistKey);
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

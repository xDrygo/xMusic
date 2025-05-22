package org.eldrygo.XMusic.Hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.eldrygo.XMusic.Managers.MusicManager;
import org.eldrygo.XMusic.XMusic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XMusicExpansion extends PlaceholderExpansion {

    private final MusicManager musicManager;
    private final XMusic plugin;

    public XMusicExpansion(MusicManager musicManager, XMusic plugin) {
        this.musicManager = musicManager;
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "xmusic";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Drygo";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier.toLowerCase()) {
            case "current_song":
                return musicManager.getCurrentSong(player).orElse("none");
            case "current_song_name":
                return musicManager.getCurrentSongName(player).orElse("none");
            case "current_song_author":
                return musicManager.getCurrentSongAuthor(player).orElse("none");
            case "current_playlist":
                return musicManager.getCurrentPlaylist(player).orElse("none");
            case "is_playing":
                return musicManager.isPlaying(player) ? "true" : "false";
            default:
                return null;
        }
    }
}

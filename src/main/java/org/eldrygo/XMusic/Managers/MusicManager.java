package org.eldrygo.XMusic.Managers;

import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.XMusic.Managers.File.ConfigManager;
import org.eldrygo.XMusic.Models.Playlist;
import org.eldrygo.XMusic.Models.PlaylistRunnable;
import org.eldrygo.XMusic.Models.SoundData;
import org.eldrygo.XMusic.Utils.ChatUtils;
import org.eldrygo.XMusic.XMusic;

import java.io.File;
import java.util.*;

public class MusicManager {

    private static ConfigManager configManager;
    private static XMusic plugin;

    private final Map<String, Playlist> playlists = new HashMap<>();
    private SoundCategory soundCategory = SoundCategory.AMBIENT;

    public static final Map<String, SoundData> soundMap = new HashMap<>();
    public static final Map<UUID, String> activePlaylists = new HashMap<>();
    public static final Map<UUID, String> currentSongs = new HashMap<>();
    public static final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();

    private FileConfiguration musicConfig;

    public MusicManager(ConfigManager configManager, XMusic plugin) {
        MusicManager.configManager = configManager;
        MusicManager.plugin = plugin;
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        loadSoundCategory();
        loadMusicFile();
        loadSounds();
        loadPlaylists();
    }

    private void loadSoundCategory() {
        String categoryName = plugin.getConfig().getString("settings.sound_category", "AMBIENT");
        try {
            soundCategory = SoundCategory.valueOf(categoryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Sound category '" + categoryName + "' is not valid. Using AMBIENT as default.");
            soundCategory = SoundCategory.AMBIENT;
        }
    }

    private void loadMusicFile() {
        File musicFile = new File(plugin.getDataFolder(), "music.yml");
        if (!musicFile.exists()) plugin.saveResource("music.yml", false);
        musicConfig = YamlConfiguration.loadConfiguration(musicFile);
    }

    public void reload() {
        loadMusicFile();
        loadSounds();
        loadPlaylists();
    }

    public void loadSounds() {
        soundMap.clear();
        if (!musicConfig.isConfigurationSection("musics")) {
            plugin.getLogger().warning("No 'musics' section found in music.yml!");
            return;
        }

        for (String key : musicConfig.getConfigurationSection("musics").getKeys(false)) {
            String path = "musics." + key;
            String sound = musicConfig.getString(path + ".sound");
            if (sound == null) {
                plugin.getLogger().warning("Sound not defined for music key: " + key);
                continue;
            }

            if (!musicConfig.contains(path + ".data.duration")) {
                plugin.getLogger().warning("Missing 'duration' in '" + path + ".data.duration'. Skipping...");
                continue;
            }

            float volume = (float) musicConfig.getDouble(path + ".volume", 1.0);
            float pitch = (float) musicConfig.getDouble(path + ".pitch", 1.0);
            String name = musicConfig.getString(path + ".data.name", key);
            String author = musicConfig.getString(path + ".data.author", "Unknown");
            int duration = musicConfig.getInt(path + ".data.duration");

            soundMap.put(key, new SoundData(sound, volume, pitch, name, author, duration));
        }
    }

    public void loadPlaylists() {
        playlists.clear();
        if (!musicConfig.isConfigurationSection("playlists")) return;

        for (String key : musicConfig.getConfigurationSection("playlists").getKeys(false)) {
            List<String> soundKeys = musicConfig.getStringList("playlists." + key + ".sounds");
            boolean shuffle = musicConfig.getBoolean("playlists." + key + ".shuffle", false);

            List<SoundData> entries = new ArrayList<>();
            for (String soundKey : soundKeys) {
                SoundData data = soundMap.get(soundKey);
                if (data != null) {
                    entries.add(data);
                } else {
                    plugin.getLogger().warning("Sound key '" + soundKey + "' in playlist '" + key + "' does not exist.");
                }
            }

            playlists.put(key, new Playlist(entries, shuffle));
        }
    }

    public void play(Player player, String key) {
        play(player, key, soundCategory);
    }

    public void play(Player player, String key, SoundCategory category) {
        SoundData data = soundMap.get(key);
        if (data == null) {
            plugin.getLogger().warning("Tried to play unknown sound key: " + key);
            return;
        }

        currentSongs.put(player.getUniqueId(), key);
        player.playSound(player.getLocation(), data.sound(), category, data.volume(), data.pitch());

        if (plugin.getConfig().getBoolean("settings.music_card_enabled", true)) {
            for (String line : configManager.getMessageConfig().getStringList("music_card")) {
                player.sendMessage(ChatUtils.formatColor(
                        line.replace("%name%", data.name()).replace("%author%", data.author())
                ));
            }
        }
    }

    public void stop(Player player, String key) {
        SoundData data = soundMap.get(key);
        if (data == null) {
            plugin.getLogger().warning("Tried to stop unknown sound key: " + key);
            return;
        }
        player.stopSound(data.sound(), soundCategory);
    }

    public void stopCurrentSong(Player player) {
        UUID playerId = player.getUniqueId();
        String currentSongKey = currentSongs.remove(playerId);
        if (currentSongKey == null) return;

        SoundData soundData = soundMap.get(currentSongKey);
        if (soundData == null) {
            plugin.getLogger().warning("Tried to stop unknown sound key: " + currentSongKey);
            return;
        }

        player.stopSound(soundData.sound(), soundCategory);
    }

    public void stopPlaylist(Player player) {
        UUID playerId = player.getUniqueId();
        String playlistKey = activePlaylists.get(playerId);
        if (playlistKey == null) return;

        Playlist playlist = playlists.get(playlistKey);
        if (playlist == null) {
            plugin.getLogger().warning("Tried to stop unknown playlist: " + playlistKey);
            activePlaylists.remove(playerId);
            return;
        }

        for (SoundData sound : playlist.sounds()) {
            player.stopSound(sound.sound(), soundCategory);
        }

        BukkitRunnable task = activeTasks.get(playerId);
        if (task != null) {
            task.cancel();
            activeTasks.remove(playerId);
        }

        activePlaylists.remove(playerId);
        currentSongs.remove(playerId);
    }

    public void stopPlaylistFinish(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable task = activeTasks.get(playerId);
        if (task instanceof PlaylistRunnable runnable) {
            runnable.setFinishOnStop(true);
        }
    }

    public boolean skipSong(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable task = activeTasks.get(playerId);
        if (task instanceof PlaylistRunnable runnable) {
            runnable.skipToNext();
            return true;
        }
        return false;
    }

    public void playPlaylist(Collection<? extends Player> players, String playlistKey) {
        playPlaylist(players, playlistKey, soundCategory);
    }

    public void playPlaylist(Collection<? extends Player> players, String playlistKey, SoundCategory category) {
        Playlist playlist = playlists.get(playlistKey);
        if (playlist == null) {
            plugin.getLogger().warning("Tried to play unknown playlist: " + playlistKey);
            return;
        }

        List<SoundData> toPlay = new ArrayList<>(playlist.sounds());
        if (playlist.shuffle()) Collections.shuffle(toPlay); // solo se hace una vez

        int globalDelaySeconds = plugin.getConfig().getInt("settings.global_delay_seconds", 0);

        for (Player player : players) {
            UUID playerId = player.getUniqueId();

            // Cancelar tarea previa si existe
            BukkitRunnable oldTask = activeTasks.remove(playerId);
            if (oldTask != null) oldTask.cancel();

            activePlaylists.put(playerId, playlistKey);

            PlaylistRunnable task = new PlaylistRunnable(player, toPlay, globalDelaySeconds, category);
            activeTasks.put(playerId, task);
            task.runTask(plugin);
        }
    }
    public static void clearPlayerState(UUID playerId) {
        activePlaylists.remove(playerId);
        activeTasks.remove(playerId);
        currentSongs.remove(playerId);
    }

    public Optional<String> getCurrentSong(Player player) {
        return Optional.ofNullable(currentSongs.get(player.getUniqueId()));
    }
    public Optional<String> getCurrentSongName(Player player) {
        String key = currentSongs.get(player.getUniqueId());
        SoundData data = soundMap.get(key);
        return data != null ? Optional.ofNullable(data.name()) : Optional.empty();
    }
    public Optional<String> getCurrentSongAuthor(Player player) {
        String key = currentSongs.get(player.getUniqueId());
        SoundData data = soundMap.get(key);
        return data != null ? Optional.ofNullable(data.author()) : Optional.empty();
    }
    public Optional<String> getCurrentPlaylist(Player player) {
        return Optional.ofNullable(activePlaylists.get(player.getUniqueId()));
    }
    public boolean isPlaying(Player player) {
        UUID playerId = player.getUniqueId();
        return activeTasks.containsKey(playerId) && currentSongs.containsKey(playerId);
    }
    public Set<String> getSoundKeys() {
        return new HashSet<>(soundMap.keySet());
    }
    public Set<String> getPlaylistKeys() {
        return new HashSet<>(playlists.keySet());
    }
    public SoundData getSoundData(String key) {
        return soundMap.get(key);
    }
    public Playlist getPlaylist(String key) {
        return playlists.get(key);
    }
    public static ConfigManager getConfigManager() {
        return configManager;
    }
    public static XMusic getPlugin() {
        return plugin;
    }
}
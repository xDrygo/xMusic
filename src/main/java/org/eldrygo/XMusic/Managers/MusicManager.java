package org.eldrygo.XMusic.Managers;

import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.XMusic.Managers.File.ConfigManager;
import org.eldrygo.XMusic.Utils.ChatUtils;
import org.eldrygo.XMusic.Utils.OtherUtils;
import org.eldrygo.XMusic.XMusic;

import java.io.File;
import java.util.*;

public class MusicManager {

    private final ConfigManager configManager;

    public record SoundData(String sound, float volume, float pitch, String name, String author, int durationSeconds) {
    }

    private final XMusic plugin;
    private final Map<String, SoundData> soundMap = new HashMap<>();
    private final Map<String, Playlist> playlists = new HashMap<>();
    private final Map<UUID, String> activePlaylists = new HashMap<>();
    private final Map<UUID, String> currentSongs = new HashMap<>();
    private final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();
    private FileConfiguration musicConfig;
    private SoundCategory soundCategory = SoundCategory.AMBIENT;

    public MusicManager(ConfigManager configManager, XMusic plugin) {
        this.configManager = configManager;
        this.plugin = plugin;

        // Asegurar que la carpeta del plugin existe
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

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

        if (!musicFile.exists()) {
            plugin.saveResource("music.yml", false);
        }

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
        player.playSound(player.getLocation(), data.sound, category, data.volume, data.pitch);

        if (plugin.getConfig().getBoolean("settings.music_card_enabled", true)) {
            List<String> messages = configManager.getMessageConfig().getStringList("music_card");

            for (String line : messages) {
                line = line
                        .replace("%name%", data.name)
                        .replace("%author%", data.author);
                player.sendMessage(ChatUtils.formatColor(line));
            }
        }
    }

    public SoundData getSoundData(String key) {
        return soundMap.get(key);
    }

    public void playPlaylist(Player player, String playlistKey) {
        Playlist playlist = playlists.get(playlistKey);
        if (playlist == null) {
            plugin.getLogger().warning("Tried to play unknown playlist: " + playlistKey);
            return;
        }

        UUID playerId = player.getUniqueId();

        // Cancelar tarea previa si existe
        if (activeTasks.containsKey(playerId)) {
            activeTasks.get(playerId).cancel();
            activeTasks.remove(playerId);
        }

        activePlaylists.put(playerId, playlistKey);
        List<SoundData> toPlay = new ArrayList<>(playlist.sounds);
        if (playlist.shuffle) {
            Collections.shuffle(toPlay);
        }

        int globalDelaySeconds = plugin.getConfig().getInt("settings.global_delay_seconds", 0);

        PlaylistRunnable task = new PlaylistRunnable(player, toPlay, globalDelaySeconds);
        activeTasks.put(playerId, task);
        task.runTask(plugin);
    }

    public Playlist getPlaylist(String key) {
        return playlists.get(key);
    }
    public void stop(Player player, String key) {
        SoundData data = soundMap.get(key);
        if (data == null) {
            plugin.getLogger().warning("Tried to stop unknown sound key: " + key);
            return;
        }
        player.stopSound(data.sound);
    }
    public void stopCurrentSong(Player player) {
        UUID playerId = player.getUniqueId();

        String currentSongKey = currentSongs.remove(playerId); // Eliminar inmediatamente
        if (currentSongKey == null) {
            return;
        }

        SoundData soundData = soundMap.get(currentSongKey);
        if (soundData == null) {
            plugin.getLogger().warning("Tried to stop unknown sound key: " + currentSongKey);
            return;
        }

        // Usa la categoría correcta si sabes con cuál fue reproducido
        player.stopSound(soundData.sound, SoundCategory.AMBIENT); // Usa la categoría que usaste al reproducir
    }


    // Para detener la playlist que está sonando en un jugador
    public void stopPlaylist(Player player) {
        UUID playerId = player.getUniqueId();
        String playlistKey = activePlaylists.get(playerId);

        if (playlistKey == null) {
            return; // No hay playlist activa para este jugador
        }

        Playlist playlist = playlists.get(playlistKey);
        if (playlist == null) {
            plugin.getLogger().warning("Tried to stop unknown playlist: " + playlistKey);
            activePlaylists.remove(playerId);
            return;
        }

        // Detener todos los sonidos de la playlist para el jugador
        for (SoundData sound : playlist.sounds) {
            player.stopSound(sound.sound);
        }

        // Cancelar la tarea que reproduce la playlist
        BukkitRunnable task = activeTasks.get(playerId);
        if (task != null) {
            task.cancel();
            activeTasks.remove(playerId);
        }

        // Limpiar el registro
        activePlaylists.remove(playerId);
    }
    public void stopPlaylistFinish(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable task = activeTasks.get(playerId);
        if (task instanceof PlaylistRunnable) {
            ((PlaylistRunnable) task).setFinishOnStop(true);
        }
    }

    public boolean skipSong(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable task = activeTasks.get(playerId);
        if (task instanceof PlaylistRunnable) {
            ((PlaylistRunnable) task).skipToNext();
            return true;
        }
        return false;
    }

    public record Playlist(List<SoundData> sounds, boolean shuffle) {
    }
    private class PlaylistRunnable extends BukkitRunnable {
        private final Player player;
        private final List<SoundData> sounds;
        private final int globalDelaySeconds;
        private int index = 0;
        private boolean finishOnStop = false;
        private SoundData currentSound = null;

        public PlaylistRunnable(Player player, List<SoundData> sounds, int globalDelaySeconds) {
            this.player = player;
            this.sounds = sounds;
            this.globalDelaySeconds = globalDelaySeconds;
        }

        @Override
        public void run() {
            UUID playerId = player.getUniqueId();

            if (!activePlaylists.containsKey(playerId)) {
                cancel();
                activeTasks.remove(playerId);
                return;
            }

            if (index >= sounds.size()) {
                cancel();
                activePlaylists.remove(playerId);
                activeTasks.remove(playerId);
                return;
            }

            // Detener canción anterior si hay
            if (currentSound != null) {
                player.stopSound(currentSound.sound);
            }

            currentSound = sounds.get(index++);
            playSoundAndShowMessage(currentSound);

            if (finishOnStop && index >= sounds.size()) {
                cancel();
                activePlaylists.remove(playerId);
                activeTasks.remove(playerId);
                return;
            }

            int delayTicks = (currentSound.durationSeconds + globalDelaySeconds) * 20;

            PlaylistRunnable newTask = new PlaylistRunnable(player, sounds, globalDelaySeconds);
            newTask.index = this.index;
            newTask.finishOnStop = this.finishOnStop;
            newTask.currentSound = this.currentSound;

            activeTasks.put(player.getUniqueId(), newTask);
            newTask.runTaskLater(plugin, delayTicks);
        }


        public void setFinishOnStop(boolean finish) {
            this.finishOnStop = finish;
        }

        public void skipToNext() {
            // Cancelar tarea actual para evitar que vuelva a ejecutar el sonido viejo
            this.cancel();
            activeTasks.remove(player.getUniqueId());

            // Detener la canción actual
            if (currentSound != null) {
                player.stopSound(currentSound.sound);
            }

            if (index < sounds.size()) {
                currentSound = sounds.get(index++);
                playSoundAndShowMessage(currentSound);

                int delayTicks = (currentSound.durationSeconds + globalDelaySeconds) * 20;

                PlaylistRunnable newTask = new PlaylistRunnable(player, sounds, globalDelaySeconds);
                newTask.index = this.index;
                newTask.finishOnStop = this.finishOnStop;
                newTask.currentSound = this.currentSound;
                newTask.runTaskLater(plugin, delayTicks);
                activeTasks.put(player.getUniqueId(), newTask);
            } else {
                activePlaylists.remove(player.getUniqueId());
            }
        }

        private void playSoundAndShowMessage(SoundData sound) {
            currentSongs.put(player.getUniqueId(), OtherUtils.getKeyByValue(soundMap, sound));
            player.playSound(player.getLocation(), sound.sound, SoundCategory.MASTER, sound.volume, sound.pitch);

            if (plugin.getConfig().getBoolean("settings.music_card_enabled", true)) {
                List<String> messages = configManager.getMessageConfig().getStringList("music_card");
                for (String line : messages) {
                    line = line.replace("%name%", sound.name)
                            .replace("%author%", sound.author);
                    player.sendMessage(ChatUtils.formatColor(line));
                }
            }
        }
    }
    public Optional<String> getCurrentSong(Player player) {
        String key = currentSongs.get(player.getUniqueId());
        if (key == null) return Optional.empty();
        return Optional.of(key);
    }

    public Optional<String> getCurrentSongName(Player player) {
        String key = currentSongs.get(player.getUniqueId());
        if (key == null) return Optional.empty();
        SoundData data = soundMap.get(key);
        if (data == null) return Optional.empty();
        return Optional.ofNullable(data.name);
    }

    public Optional<String> getCurrentSongAuthor(Player player) {
        String key = currentSongs.get(player.getUniqueId());
        if (key == null) return Optional.empty();
        SoundData data = soundMap.get(key);
        if (data == null) return Optional.empty();
        return Optional.ofNullable(data.author);
    }

    public Optional<String> getCurrentPlaylist(Player player) {
        String playlistKey = activePlaylists.get(player.getUniqueId());
        if (playlistKey == null) return Optional.empty();
        return Optional.of(playlistKey);
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
}
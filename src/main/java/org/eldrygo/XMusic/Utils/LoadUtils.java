package org.eldrygo.XMusic.Utils;

import org.bukkit.Bukkit;
import org.eldrygo.XMusic.Handlers.XMusicCommand;
import org.eldrygo.XMusic.Handlers.XMusicTabCompleter;
import org.eldrygo.XMusic.Hooks.XMusicExpansion;
import org.eldrygo.XMusic.Managers.File.ConfigManager;
import org.eldrygo.XMusic.Managers.MusicManager;
import org.eldrygo.XMusic.XMusic;

public class LoadUtils {
    private final ConfigManager configManager;
    private final XMusic plugin;
    private final MusicManager musicManager;
    private final ChatUtils chatUtils;

    public LoadUtils(ConfigManager configManager, XMusic plugin, MusicManager musicManager, ChatUtils chatUtils) {
        this.configManager = configManager;
        this.plugin = plugin;
        this.musicManager = musicManager;
        this.chatUtils = chatUtils;
    }
    public void loadFeatures() {
        loadFiles();
        loadCommand();
        loadPlaceholderAPI();
    }
    public void loadFiles() {
        configManager.loadConfig();
        configManager.reloadMessages();
        configManager.setPrefix(configManager.getMessageConfig().getString("prefix"));
        musicManager.reload();
    };
    private void loadCommand() {
        if (plugin.getCommand("xmusic") != null) {
            plugin.getLogger().info("✅ Plugin command /xmusic successfully registered.");
            plugin.getCommand("xmusic").setExecutor(new XMusicCommand(musicManager, chatUtils, this, configManager, plugin));
            plugin.getCommand("xmusic").setTabCompleter(new XMusicTabCompleter(musicManager));
        } else {
            plugin.getLogger().severe("❌ Error: /xmusic command is no registered in plugin.yml");
        }
    }
    private void loadPlaceholderAPI() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new XMusicExpansion(musicManager, plugin).register();
            plugin.getLogger().info("✅ PlaceholderAPI detected. PAPI dependency successfully loaded.");
        } else {
            plugin.getLogger().warning("⚠  PlaceholderAPI not detected. PAPI placeholders will not work.");
        }
    }
}

package org.eldrygo.XMusic;

import org.bukkit.plugin.java.JavaPlugin;
import org.eldrygo.XMusic.API.XMusicAPI;
import org.eldrygo.XMusic.Managers.File.ConfigManager;
import org.eldrygo.XMusic.Managers.MusicManager;
import org.eldrygo.XMusic.Utils.ChatUtils;
import org.eldrygo.XMusic.Utils.LoadUtils;
import org.eldrygo.XMusic.Utils.LogsUtils;

public class XMusic extends JavaPlugin {
    public String prefix;
    public String version;
    private LogsUtils logsUtils;

    @Override
    public void onEnable() {
        version = getDescription().getVersion();
        ConfigManager configManager = new ConfigManager(this);
        ChatUtils chatUtils = new ChatUtils(this, configManager);
        MusicManager musicManager = new MusicManager(configManager, this);
        LoadUtils loadUtils = new LoadUtils(configManager, this, musicManager, chatUtils);
        this.logsUtils = new LogsUtils(this);
        XMusicAPI.init(musicManager);

        loadUtils.loadFeatures();
        logsUtils.sendStartupMessage();
    }

    @Override
    public void onDisable() {
        logsUtils.sendShutdownMessage();
    }
}

package org.eldrygo.XMusic.Handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.eldrygo.XMusic.Managers.File.ConfigManager;
import org.eldrygo.XMusic.Managers.MusicManager;
import org.eldrygo.XMusic.Utils.ChatUtils;
import org.eldrygo.XMusic.Utils.LoadUtils;
import org.eldrygo.XMusic.XMusic;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class XMusicCommand implements CommandExecutor {

    private final MusicManager musicManager;
    private final ChatUtils chatUtils;
    private final LoadUtils loadUtils;
    private final ConfigManager configManager;
    private final XMusic plugin;

    public XMusicCommand(MusicManager musicManager, ChatUtils chatUtils, LoadUtils loadUtils, ConfigManager configManager, XMusic plugin) {
        this.musicManager = musicManager;
        this.chatUtils = chatUtils;
        this.loadUtils = loadUtils;
        this.configManager = configManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (args.length < 1) {
            sender.sendMessage(chatUtils.getMessage("error.unknown_command", null));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "play" -> {
                return handlePlay(sender, args, label);
            }
            case "stop" -> {
                return handleStop(sender, args, label);
            }
            case "skip" -> {
                return handleSkip(sender, args, label);
            }
            case "reload" -> handleReload(sender, label);
            case "help" -> {
                if (!sender.hasPermission("xmusic.command.help") && !sender.hasPermission("xmusic.admin") && !sender.isOp()) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", null));
                    return true;
                }
                List<String> helpMessage = configManager.getMessageConfig().getStringList("command.help");
                for (String line : helpMessage) {
                    sender.sendMessage(ChatUtils.formatColor(line));
                }
                return true;
            }
            case "info" -> {
                if (!sender.hasPermission("xmusic.command.info") && !sender.hasPermission("xmusic.admin") && !sender.isOp()) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", null));
                    return true;
                }
                infoXMusic(sender);
            }
            default -> {
                sender.sendMessage(chatUtils.getMessage("error.unknown_command", null));
                return true;
            }
        }
        return false;
    }

    private boolean handlePlay(CommandSender sender, String[] args, String label) {
        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("command.play.usage", null));
            return true;
        }

        String type = args[1].toLowerCase();
        String key = args[2];

        // Comprobación de permisos
        if (type.equals("song") && !(
                sender.hasPermission("xmusic.command.stop.song") ||
                        sender.hasPermission("xmusic.command.stop") ||
                        sender.hasPermission("xmusic.admin") ||
                        sender.isOp())) {
            sender.sendMessage(chatUtils.getMessage("error.no_permission", null).replace("%command%", label));
            return true;
        }

        if (type.equals("playlist") && !(
                sender.hasPermission("xmusic.command.stop.song") ||
                        sender.hasPermission("xmusic.command.stop") ||
                        sender.hasPermission("xmusic.admin") ||
                        sender.isOp())) {
            sender.sendMessage(chatUtils.getMessage("error.no_permission", null).replace("%command%", label));
            return true;
        }

        List<Player> targets = new ArrayList<>();
        boolean isBroadcast = false;
        String targetName = "";

        if (args.length >= 4) {
            targetName = args[3];
            if (targetName.equals("*")) {
                targets.addAll(Bukkit.getOnlinePlayers());
                isBroadcast = true;
            } else {
                Player target = Bukkit.getPlayerExact(targetName);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(chatUtils.getMessage("command.play.player_invalid", null)
                            .replace("%target%", targetName));
                    return true;
                }
                targets.add(target);
            }
        } else {
            if (sender instanceof Player) {
                targets.add((Player) sender);
                targetName = sender.getName();
            } else {
                sender.sendMessage(chatUtils.getMessage("command.play.usage", null));
                return true;
            }
        }

        if (type.equals("song")) {
            if (!musicManager.getSoundKeys().contains(key)) {
                sender.sendMessage(chatUtils.getMessage("command.play.not_found", null)
                        .replace("%type%", "song").replace("%key%", key));
                return true;
            }
            for (Player player : targets) {
                musicManager.play(player, key);
            }
        } else if (type.equals("playlist")) {
            if (!musicManager.getPlaylistKeys().contains(key)) {
                sender.sendMessage(chatUtils.getMessage("command.play.not_found", null)
                        .replace("%type%", "playlist").replace("%key%", key));
                return true;
            }
            for (Player player : targets) {
                musicManager.playPlaylist(player, key);
            }
        } else {
            sender.sendMessage(chatUtils.getMessage("command.play.type_invalid", null));
            return true;
        }

        // Envío de mensaje al final
        if (isBroadcast) {
            sender.sendMessage(chatUtils.getMessage("command.play.success_all", null)
                    .replace("%song%", key));
        } else if (sender instanceof Player && targets.size() == 1 && ((Player) sender).getUniqueId().equals(targets.get(0).getUniqueId())) {
            sender.sendMessage(chatUtils.getMessage("command.play.self", null)
                    .replace("%song%", key));
        } else {
            sender.sendMessage(chatUtils.getMessage("command.play.target", null)
                    .replace("%song%", key)
                    .replace("%target%", targets.get(0).getName()));
        }

        return true;
    }


    private boolean handleStop(CommandSender sender, String[] args, String label) {
        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("command.stop.usage", null));
            return true;
        }

        String type = args[1].toLowerCase();
        String targetName = args[2];
        boolean finish = args.length >= 4 && args[3].equalsIgnoreCase("finish");

        // Permisos
        if (type.equals("song") && !(
                sender.hasPermission("xmusic.command.stop.song") ||
                        sender.hasPermission("xmusic.command.stop") ||
                        sender.hasPermission("xmusic.admin") ||
                        sender.isOp())) {
            sender.sendMessage(chatUtils.getMessage("error.no_permission", null).replace("%command%", label));
            return true;
        }

        if (type.equals("playlist") && !(
                sender.hasPermission("xmusic.command.stop.playlist") ||
                        sender.hasPermission("xmusic.command.stop") ||
                        sender.hasPermission("xmusic.admin") ||
                        sender.isOp())) {
            sender.sendMessage(chatUtils.getMessage("error.no_permission", null).replace("%command%", label));
            return true;
        }

        List<Player> targets = new ArrayList<>();
        String displayTarget;

        if (targetName.equals("*")) {
            targets.addAll(Bukkit.getOnlinePlayers());
            displayTarget = "everyone";
        } else {
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(chatUtils.getMessage("command.stop.player_invalid", null)
                        .replace("%target%", targetName));
                return true;
            }
            targets.add(target);
            displayTarget = targetName;
        }

        // Acción
        if (type.equals("song")) {
            for (Player player : targets) {
                musicManager.stopCurrentSong(player);
            }
            sender.sendMessage(chatUtils.getMessage("command.stop.song", null)
                    .replace("%target%", displayTarget));

        } else if (type.equals("playlist")) {
            if (finish) {
                for (Player player : targets) {
                    musicManager.stopPlaylistFinish(player);
                }
                sender.sendMessage(chatUtils.getMessage("command.stop.playlist_finish", null)
                        .replace("%target%", displayTarget));
            } else {
                for (Player player : targets) {
                    musicManager.stopPlaylist(player);
                }
                sender.sendMessage(chatUtils.getMessage("command.stop.playlist", null)
                        .replace("%target%", displayTarget));
            }
        } else {
            sender.sendMessage(chatUtils.getMessage("command.stop.type_invalid", null));
            return true;
        }

        return true;
    }


    private boolean handleSkip(CommandSender sender, String[] args, String label) {
        if (!(sender.hasPermission("xmusic.command.skip") || sender.hasPermission("xmusic.admin") || sender.isOp())) {
            sender.sendMessage(chatUtils.getMessage("error.no_permission", null).replace("%command%", label));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(chatUtils.getMessage("command.skip.usage", null));
            return true;
        }

        String targetName = args[1];
        List<Player> targets = new ArrayList<>();

        if (targetName.equals("*")) {
            targets.addAll(Bukkit.getOnlinePlayers());
        } else {
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(chatUtils.getMessage("command.skip.player_invalid", null)
                        .replace("%target%", targetName));
                return true;
            }
            targets.add(target);
        }

        if (targetName.equals("*")) {
            int skippedCount = 0;
            int failedCount = 0;

            for (Player player : targets) {
                boolean skipped = musicManager.skipSong(player);
                if (skipped) {
                    skippedCount++;
                } else {
                    failedCount++;
                }
            }

            sender.sendMessage(chatUtils.getMessage("command.skip.summary", null)
                    .replace("%success%", String.valueOf(skippedCount))
                    .replace("%fail%", String.valueOf(failedCount)));

        } else {
            Player target = targets.iterator().next(); // solo uno si no es "*"
            boolean skipped = musicManager.skipSong(target);
            String messageKey = skipped ? "command.skip.success" : "command.skip.fail";
            sender.sendMessage(chatUtils.getMessage(messageKey, null)
                    .replace("%target%", targetName));
        }

        return true;
    }

    private void handleReload(CommandSender sender, String label) {
        if (!(sender.hasPermission("xmusic.command.reload") && sender.isOp())) {
            sender.sendMessage(chatUtils.getMessage("error.no_permission", null).replace("%command%", label));
            return;
        }

        Player target = (sender instanceof Player) ? (Player) sender : null;
        try {
            loadUtils.loadFiles();
        } catch (Exception e) {
            sender.sendMessage(chatUtils.getMessage("command.reload.error", target));
            return;
        }
        sender.sendMessage(chatUtils.getMessage("command.reload.success", target));
    }
    private void infoXMusic(CommandSender sender) {
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("&8                              #ba5bff&lx&r&f&lMusic &8» &r&fInfo"));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("#fff18d&l                           ᴍᴀᴅᴇ ʙʏ"));
        sender.sendMessage(ChatUtils.formatColor("&f                           Drygo #707070» &7&o(@eldrygo)"));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("#fff18d&l                  ʀᴜɴɴɪɴɢ ᴘʟᴜɢɪɴ ᴠᴇʀꜱɪᴏɴ"));
        sender.sendMessage(ChatUtils.formatColor("&f                                    " + plugin.version));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("#fff18d&l               ᴅʀʏɢᴏ'ꜱ ɴᴏᴛᴇ ᴏꜰ ᴛʜᴇ ᴠᴇʀꜱɪᴏɴ"));
        sender.sendMessage(ChatUtils.formatColor("&f  #FFFAAB          Welcome to xMusic! I made this plugin because"));
        sender.sendMessage(ChatUtils.formatColor("&f  #FFFAAB       this was a function with I have so many problems"));
        sender.sendMessage(ChatUtils.formatColor("&f  #FFFAAB           before, so I tried to make a plugin for this."));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("&7"));
    }
}

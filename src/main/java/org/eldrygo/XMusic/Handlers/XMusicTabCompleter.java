package org.eldrygo.XMusic.Handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.eldrygo.XMusic.Managers.MusicManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class XMusicTabCompleter implements TabCompleter {

    private final MusicManager musicManager;

    public XMusicTabCompleter(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            return filter(List.of("play", "stop", "skip", "reload", "help", "info"), args[0]);
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "play" -> {
                if (args.length == 2) {
                    return filter(List.of("song", "playlist"), args[1]);
                } else if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("song")) {
                        return filter(new ArrayList<>(musicManager.getSoundKeys()), args[2]);
                    } else if (args[1].equalsIgnoreCase("playlist")) {
                        return filter(new ArrayList<>(musicManager.getPlaylistKeys()), args[2]);
                    }
                } else if (args.length == 4) {
                    List<String> names = Bukkit.getOnlinePlayers().stream()
                            .map(p -> p.getName())
                            .collect(Collectors.toList());
                    names.add("*");
                    return filter(names, args[3]);
                }
            }

            case "stop" -> {
                if (args.length == 2) {
                    return filter(List.of("song", "playlist"), args[1]);
                } else if (args.length == 3) {
                    List<String> names = Bukkit.getOnlinePlayers().stream()
                            .map(p -> p.getName())
                            .collect(Collectors.toList());
                    names.add("*");
                    return filter(names, args[2]);
                } else if (args.length == 4 && args[1].equalsIgnoreCase("playlist")) {
                    return filter(List.of("finish"), args[3]);
                }
            }

            case "skip" -> {
                if (args.length == 2) {
                    List<String> names = Bukkit.getOnlinePlayers().stream()
                            .map(p -> p.getName())
                            .collect(Collectors.toList());
                    names.add("*");
                    return filter(names, args[1]);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String current) {
        return options.stream()
                .filter(opt -> opt.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }
}

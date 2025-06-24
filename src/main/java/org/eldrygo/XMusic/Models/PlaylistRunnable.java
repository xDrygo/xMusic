package org.eldrygo.XMusic.Models;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.XMusic.Managers.MusicManager;
import org.eldrygo.XMusic.Utils.ChatUtils;
import org.eldrygo.XMusic.Utils.OtherUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlaylistRunnable extends BukkitRunnable {

    private final Player player;
    private final List<SoundData> sounds;
    private final int globalDelaySeconds;
    private final SoundCategory category;

    private int index = 0;
    private boolean finishOnStop = false;
    private SoundData currentSound = null;

    public PlaylistRunnable(@NotNull Player player, @NotNull List<SoundData> sounds, int globalDelaySeconds, @NotNull SoundCategory category) {
        this.player = player;
        this.sounds = sounds;
        this.globalDelaySeconds = globalDelaySeconds;
        this.category = category;
    }

    @Override
    public void run() {
        if (MusicManager.activeTasks.get(player.getUniqueId()) != this) {
            cancel();
            return;
        }

        UUID playerId = player.getUniqueId();
        if (!MusicManager.activePlaylists.containsKey(playerId) || index >= sounds.size()) {
            cancel();
            MusicManager.clearPlayerState(playerId);
            return;
        }

        if (currentSound != null) {
            player.stopSound(currentSound.sound(), category);
        }

        currentSound = sounds.get(index++);
        playSoundAndShowMessage(currentSound);

        if (finishOnStop && index >= sounds.size()) {
            cancel();
            MusicManager.clearPlayerState(playerId);
            return;
        }

        int delayTicks = (currentSound.durationSeconds() + globalDelaySeconds) * 20;

        scheduleNext(delayTicks);
    }

    public void setFinishOnStop(boolean finish) {
        this.finishOnStop = finish;
    }

    public void skipToNext() {
        this.cancel();
        MusicManager.activeTasks.remove(player.getUniqueId());

        if (currentSound != null) {
            player.stopSound(currentSound.sound(), category);
        }

        if (index < sounds.size()) {
            currentSound = sounds.get(index++);
            playSoundAndShowMessage(currentSound);

            int delayTicks = (currentSound.durationSeconds() + globalDelaySeconds) * 20;

            scheduleNext(delayTicks);
        } else {
            MusicManager.activePlaylists.remove(player.getUniqueId());
            MusicManager.currentSongs.remove(player.getUniqueId());
        }
    }

    private void playSoundAndShowMessage(SoundData sound) {
        MusicManager.currentSongs.put(player.getUniqueId(), OtherUtils.getKeyByValue(MusicManager.soundMap, sound));
        player.playSound(player.getLocation(), sound.sound(), category, sound.volume(), sound.pitch());

        if (MusicManager.getPlugin().getConfig().getBoolean("settings.music_card_enabled", true)) {
            for (String line : MusicManager.getConfigManager().getMessageConfig().getStringList("music_card")) {
                player.sendMessage(ChatUtils.formatColor(
                        line.replace("%name%", sound.name()).replace("%author%", sound.author())
                ));
            }
        }
    }

    private void scheduleNext(int delayTicks) {
        PlaylistRunnable newTask = new PlaylistRunnable(player, sounds, globalDelaySeconds, category);
        newTask.index = this.index;
        newTask.finishOnStop = this.finishOnStop;
        newTask.currentSound = this.currentSound;
        MusicManager.activeTasks.put(player.getUniqueId(), newTask);
        newTask.runTaskLater(MusicManager.getPlugin(), delayTicks);
    }
}

// src/main/java/org/Foxraft/battleRoyale/managers/StormManager.java
package org.Foxraft.battleRoyale.managers;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.Foxraft.battleRoyale.events.StormReachedFinalDestinationEvent;

import java.util.Objects;

public class StormManager {
    private final JavaPlugin plugin;
    private final double stormSpeed;
    private final double mapRadius;
    private BukkitRunnable damageTask;

    public StormManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.stormSpeed = plugin.getConfig().getDouble("stormSpeed", 0.5); // blocks per second
        this.mapRadius = plugin.getConfig().getDouble("mapRadius", 500);
    }

    public void startStorm() {
        double finalRadius = 50;
        double shrinkDuration = (mapRadius - finalRadius) / stormSpeed; // duration in seconds

        WorldBorder worldBorder = Objects.requireNonNull(Bukkit.getWorld("world")).getWorldBorder();
        worldBorder.setCenter(0, 0);
        worldBorder.setSize(mapRadius * 2); // radius *2 = diameter
        worldBorder.setSize(finalRadius * 2, (long) shrinkDuration);

        startStormShrinking(worldBorder);
    }

    public void stopStorm() {
        WorldBorder worldBorder = Objects.requireNonNull(Bukkit.getWorld("world")).getWorldBorder();
        worldBorder.setSize(mapRadius * 2);
        if (damageTask != null) {
            damageTask.cancel();
            Bukkit.getLogger().info("Storm task got manually cancelled.");
        }
    }

    private void startStormShrinking(WorldBorder worldBorder) {
        damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                double finalRadius = 50; // death match area

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!worldBorder.isInside(player.getLocation())) {
                        player.damage(1.0);
                        player.playNote(player.getLocation(), Instrument.IRON_XYLOPHONE, Note.natural(1, Note.Tone.G));
                        player.playNote(player.getLocation(), Instrument.IRON_XYLOPHONE, Note.natural(1, Note.Tone.A));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED +"You are in the storm!"));
                    }
                }

                // Check if the border has reached its final size
                if (worldBorder.getSize() <= finalRadius * 2) {
                    Bukkit.getPluginManager().callEvent(new StormReachedFinalDestinationEvent());
                    this.cancel();
                }
            }
        };
        damageTask.runTaskTimer(plugin, 0L, 20L); // 1s
    }
    public void resetBorder(){
        WorldBorder worldBorder = Objects.requireNonNull(Bukkit.getWorld("world")).getWorldBorder();
        worldBorder.setSize(mapRadius * 2);
    }

    public int calculateStormDuration() {
        return (int) ((mapRadius - 50) / stormSpeed);
    }
}
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
    private final String worldName;
    private BukkitRunnable damageTask;

    public StormManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.stormSpeed = plugin.getConfig().getDouble("stormSpeed", 0.5); // blocks per second
        this.mapRadius = plugin.getConfig().getDouble("mapRadius", 500);
        this.worldName = plugin.getConfig().getString("lobby.world", "world");
    }

    public void startStorm() {
        double finalRadius = 50;
        double shrinkDuration = (mapRadius - finalRadius) / stormSpeed; // duration in seconds

        WorldBorder worldBorder = Objects.requireNonNull(Bukkit.getWorld(worldName)).getWorldBorder();
        worldBorder.setCenter(0, 0);
        worldBorder.setSize(mapRadius * 2); // radius *2 = diameter
        worldBorder.setSize(finalRadius * 2, (long) shrinkDuration);

        startStormShrinking(worldBorder);
    }

    public void stopStorm() {
            WorldBorder worldBorder = Objects.requireNonNull(Bukkit.getWorld(worldName)).getWorldBorder();
        worldBorder.setSize(mapRadius * 2);
        if (damageTask != null) {
            damageTask.cancel();
            Bukkit.getLogger().info("Storm task got manually cancelled.");
        }
    }

    private void startStormShrinking(WorldBorder worldBorder) {
        World gameWorld = Bukkit.getWorld(worldName);
        damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                double finalRadius = 50; // death match area

                assert gameWorld != null;
                for (Player player : gameWorld.getPlayers()) {
                    if (!worldBorder.isInside(player.getLocation())) {
                        player.damage(1.0);
                        player.playNote(player.getLocation(), Instrument.IRON_XYLOPHONE, Note.natural(1, Note.Tone.G));
                        player.playNote(player.getLocation(), Instrument.IRON_XYLOPHONE, Note.natural(1, Note.Tone.A));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You are in the storm!"));
                    }
                }

                if (worldBorder.getSize() <= finalRadius * 2) {
                    Bukkit.getPluginManager().callEvent(new StormReachedFinalDestinationEvent());
                    this.cancel();
                }
            }
        };
        damageTask.runTaskTimer(plugin, 0L, 20L); // 1s
    }
    public void resetBorder() {
        // smol delay to avoid exception
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World '" + worldName + "' not found for border reset.");
                return;
            }
            WorldBorder border = world.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(mapRadius*2);
        }, 20L); // 1s
    }

    public int calculateStormDuration() {
        return (int) ((mapRadius - 50) / stormSpeed);
    }
}
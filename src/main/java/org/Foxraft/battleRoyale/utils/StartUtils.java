package org.Foxraft.battleRoyale.utils;

import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.models.SpawnPoint;
import org.Foxraft.battleRoyale.models.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class StartUtils {
    private final JavaPlugin plugin;
    private final TeamManager teamManager;
    private final List<SpawnPoint> spawnPoints;
    private final Location fallbackSpawn;

    public StartUtils(JavaPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.spawnPoints = new ArrayList<>();
        this.fallbackSpawn = createFallbackSpawn();
    }

    public void prepareSpawnPoints() {
        spawnPoints.clear();
        World world = Objects.requireNonNull(Bukkit.getWorld(plugin.getConfig().getString("lobby.world", "world")));
        int mapRadius = plugin.getConfig().getInt("mapRadius");
        int teamCount = teamManager.getTeams().size();
        double distance = mapRadius * 0.6;

        //generate spawn points (circular)
        for (int i = 0; i < teamCount; i++) {
            double angle = (2 * Math.PI * i) / teamCount;
            int x = (int) (distance * Math.cos(angle));
            int z = (int) (distance * Math.sin(angle));

            //load
            world.getChunkAt(x >> 4, z >> 4).load(true);
            int y = world.getHighestBlockYAt(x, z) + 1;

            Location loc = new Location(world, x + 0.5, y, z + 0.5,
                    (float) Math.toDegrees(angle) + 90, 0);
            spawnPoints.add(new SpawnPoint(loc));
        }
    }

    public void teleportTeams() {
        if (spawnPoints.isEmpty()) {
            prepareSpawnPoints();
        }

        int spawnIndex = 0;
        for (Team team : teamManager.getTeams().values()) {
            Location teamSpawn = spawnIndex < spawnPoints.size()
                    ? spawnPoints.get(spawnIndex++).getLocation()
                    : fallbackSpawn;

            for (String playerName : team.getPlayers()) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    player.teleport(teamSpawn);
                }
            }
        }
    }

    private Location createFallbackSpawn() {
        World world = Bukkit.getWorld(plugin.getConfig().getString("lobby.world", "world"));
        if (world != null) {
            int y = world.getHighestBlockYAt(0, 0) + 1;
            return new Location(world, 0.5, y, 0.5);
        }
        return new Location(Bukkit.getWorlds().get(0), 0, 100, 0);
    }

    public void broadcastCountdown(int seconds) {
        for (int i = 0; i <= seconds; i++) {
            final int count = seconds - i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (count > 0) {
                        player.sendTitle(ChatColor.YELLOW + "Game starts in", ChatColor.RED + String.valueOf(count), 10, 20, 10);
                        player.playSound(player.getLocation(), "minecraft:block.note_block.pling", 1.0f, 1.0f);
                    } else {
                        player.sendTitle(ChatColor.GREEN + "Good luck!", "", 10, 20, 10);
                        player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);                    }
                }
            }, 20L * i);
        }
    }
}
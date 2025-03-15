package org.Foxraft.battleRoyale.utils;

import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StartUtils {
    private final JavaPlugin plugin;
    private final TeamManager teamManager;
    private List<Location> spawnLocations;

    public StartUtils(JavaPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    public void generateSpawnLocationsAndTeleportTeams() {
        int mapRadius = plugin.getConfig().getInt("mapRadius");
        spawnLocations = generateSpawnLocations(mapRadius, teamManager.getTeams().size());
    }

    public void teleportTeamsToSpawnLocations() {
        int index = 0;
        for (Team team : teamManager.getTeams().values()) {
            if (index < spawnLocations.size()) {
                Location spawnLocation = spawnLocations.get(index++);
                Bukkit.getLogger().info("Teleporting team " + team.getName() + " to location: " + spawnLocation);
                for (String playerName : team.getPlayers()) {
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        Bukkit.getLogger().info("Teleporting player " + playerName + " to location: " + spawnLocation);
                        player.teleport(spawnLocation);
                    } else {
                        Bukkit.getLogger().warning("Player " + playerName + " is not online and cannot be teleported.");
                    }
                }
            } else {
                Bukkit.getLogger().warning("Not enough spawn locations for all teams.");
                break;
            }
        }
    }
    //TODO optimize this method
    private List<Location> generateSpawnLocations(int mapRadius, int teamCount) {
        List<Location> locations = new ArrayList<>();
        double angleIncrement = 2 * Math.PI / teamCount;
        double distance = mapRadius - 48; // Subtract 3 chunks (48 blocks) from the map radius

        for (int i = 0; i < teamCount; i++) {
            double angle = i * angleIncrement;
            double x = distance * Math.cos(angle);
            double z = distance * Math.sin(angle);
            Location location = new Location(Bukkit.getWorld("world"), x, 0, z);
            int highestY = Objects.requireNonNull(location.getWorld()).getHighestBlockYAt(location);
            Location spawnLocation = new Location(location.getWorld(), x, highestY + 2, z); // Increment Y by 2 to ensure players spawn on top of the block
            Bukkit.getLogger().info("Generated spawn location for team " + (i + 1) + ": " + spawnLocation + " (highest Y: " + highestY + ")");
            locations.add(spawnLocation);
        }
        return locations;
    }

    public void broadcastCountdown(int seconds) {
        for (int i = 0; i <= seconds; i++) {
            final int count = seconds - i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (count > 0) {
                        player.sendTitle(ChatColor.YELLOW + "Game starts in", ChatColor.RED + String.valueOf(count), 10, 20, 10);
                        player.playSound(player.getLocation(), "minecraft:block.note_block.pling", 1.0f, 1.0f); // Ding sound
                    } else {
                        player.sendTitle(ChatColor.GREEN + "Good luck!", "", 10, 20, 10);
                        player.playSound(player.getLocation(), "minecraft:entity.ender_dragon.growl", 1.0f, 1.0f); // Dragon scream sound
                    }
                }
            }, 20L * i);
        }
    }
}
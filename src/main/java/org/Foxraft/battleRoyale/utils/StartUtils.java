package org.Foxraft.battleRoyale.utils;

import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.models.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StartUtils {
    private final JavaPlugin plugin;
    private final TeamManager teamManager;
    private List<Location> spawnLocations;
    private String worldName;

    public StartUtils(JavaPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.worldName = plugin.getConfig().getString("lobby.world", "world");
    }

    public void generateSpawnLocationsAndTeleportTeams() {
        int mapRadius = plugin.getConfig().getInt("mapRadius");
        spawnLocations = generateSpawnLocations(mapRadius, teamManager.getTeams().size());
    }

    public void teleportTeamsToSpawnLocations() {
        int index = 0;
        for (Team team : teamManager.getTeams().values()) {
            if (index <= spawnLocations.size()) {
                Location spawnLocation = spawnLocations.get(index++);
                for (String playerName : team.getPlayers()) {
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        player.teleport(spawnLocation);
                    }
                }
            } else {
                Bukkit.getLogger().warning("Not enough spawn locations for all teams.");
                break;
            }
        }
    }
    private List<Location> generateSpawnLocations(int mapRadius, int teamCount) {
        List<Location> locations = new ArrayList<>(teamCount);
        String worldName = plugin.getConfig().getString("lobby.world", "world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            Bukkit.getLogger().severe("Could not find world. Make sure the lobby is set.");
            return locations;
        }

        double distance = Math.max(mapRadius * 0.6, mapRadius - 48);
        double angleIncrement = 2 * Math.PI / teamCount;
        int maxAttempts = 3;

        for (int i = 0; i < teamCount; i++) {
            Location spawnLoc = null;
            int attempts = 0;

            while (spawnLoc == null && attempts < maxAttempts) {
                double angle = i * angleIncrement + (attempts * (Math.PI / 8));
                int x = (int) (distance * Math.cos(angle));
                int z = (int) (distance * Math.sin(angle));

                Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                if (!chunk.isLoaded()) {
                    chunk.load(true);
                }

                int highestY = world.getHighestBlockYAt(x, z);
                Location potential = new Location(world, x, highestY + 1, z);

                if (isSafeLocation(potential)) {
                    spawnLoc = potential.clone().add(0.5, 1, 0.5);
                    spawnLoc.setYaw((float) Math.toDegrees(angle) + 90);
                    break;
                }
                attempts++;
            }

            if (spawnLoc != null) {
                locations.add(spawnLoc);
            } else {
                Bukkit.getLogger().warning("Could not find safe location for team " + (i + 1));
            }
        }

        return locations;
    }

    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        assert world != null;
        if (!world.getBlockAt(x, y, z).getType().isAir() ||
                !world.getBlockAt(x, y + 1, z).getType().isAir()) {
            return false;
        }

        Material below = world.getBlockAt(x, y - 1, z).getType();
        return below.isSolid() && !below.toString().contains("LAVA");
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
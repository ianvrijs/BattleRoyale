package org.Foxraft.battleRoyale.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StatsManager {
    private final Plugin plugin;
    private final File statsFile;
    private FileConfiguration statsConfig;
    private final Map<String, Integer> killsCache;
    private final int POINTS_PER_KILL = 25;

    public StatsManager(Plugin plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        this.killsCache = new HashMap<>();
        loadStats();
    }

    private void loadStats() {
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create stats file: " + statsFile);
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }

    private String sanitizePlayerName(String name) {
        return name.replace(".", "_").replace("*", "BEDROCK_");
    }

    public void addKill(Player player) {
        String playerName = sanitizePlayerName(player.getName());
        int currentKills = getKills(player);
        killsCache.put(playerName, currentKills + 1);
        saveStats();
    }

    public int getKills(Player player) {
        String playerName = sanitizePlayerName(player.getName());
        if (killsCache.containsKey(playerName)) {
            return killsCache.get(playerName);
        }
        return statsConfig.getInt("players." + playerName + ".kills", 0);
    }

    public int getPoints(Player player) {
        return getKills(player) * POINTS_PER_KILL;
    }

    public void saveStats() {
        for (Map.Entry<String, Integer> entry : killsCache.entrySet()) {
            String path = "players." + entry.getKey();
            statsConfig.set(path + ".kills", entry.getValue());
            statsConfig.set(path + ".points", entry.getValue() * POINTS_PER_KILL);
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save stats to " + statsFile);
        }
    }

    public void resetStats(Player player) {
        String playerName = sanitizePlayerName(player.getName());
        killsCache.remove(playerName);
        statsConfig.set("players." + playerName, null);
        saveStats();
        player.sendMessage(ChatColor.YELLOW + "Your stats have been reset by an admin.");
    }
    public void clearAllStats() {
        killsCache.clear();
        statsConfig.set("players", null);
        saveStats();
    }
}
package org.Foxraft.battleRoyale.states.gulag;

import org.Foxraft.battleRoyale.states.gulag.GulagState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
/**
 * This class manages the Gulag state and handles player enlistment and fights in the Gulag.
 * Dependencies: PlayerManager, Player, GulagState, PlayerState, Location, JavaPlugin
 */
public class GulagManager {
    private final PlayerManager playerManager;
    private final Location gulagLocation1;
    private final Location gulagLocation2;
    private final Location lobbyLocation;
    private final Location defaultRespawnLocation;
    private final Queue<Player> gulagQueue = new LinkedList<>();
    private GulagState gulagState = GulagState.IDLE;
    private final int eliminationYLevel;

    public GulagManager(PlayerManager playerManager, JavaPlugin plugin) {
        this.playerManager = playerManager;
        this.gulagLocation1 = getLocationFromConfig(plugin, "gulag1", null);
        this.gulagLocation2 = getLocationFromConfig(plugin, "gulag2", null);
        this.lobbyLocation = getLocationFromConfig(plugin, "lobby", null);
        this.defaultRespawnLocation = new Location(Bukkit.getWorld("world"), 0, Objects.requireNonNull(Bukkit.getWorld("world")).getHighestBlockYAt(0, 0) + 2, 0);
        this.eliminationYLevel = plugin.getConfig().getInt("gulag.eliminationYLevel", 0);
    }

    private Location getLocationFromConfig(JavaPlugin plugin, String path, Location defaultLocation) {
        if (!plugin.getConfig().contains(path)) {
            return defaultLocation;
        }
        String world = plugin.getConfig().getString(path + ".world", defaultLocation != null ? Objects.requireNonNull(defaultLocation.getWorld()).getName() : "world");
        double x = plugin.getConfig().getDouble(path + ".x", defaultLocation != null ? defaultLocation.getX() : 0);
        double y = plugin.getConfig().getDouble(path + ".y", defaultLocation != null ? defaultLocation.getY() : 64);
        double z = plugin.getConfig().getDouble(path + ".z", defaultLocation != null ? defaultLocation.getZ() : 0);
        float yaw = (float) plugin.getConfig().getDouble(path + ".yaw", defaultLocation != null ? defaultLocation.getYaw() : 0);
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch", defaultLocation != null ? defaultLocation.getPitch() : 0);
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public void enlistInGulag(Player player) {
        playerManager.setPlayerState(player, PlayerState.GULAG);
        playerManager.setEnteredGulag(player, true);
        gulagQueue.add(player);
        player.teleport(gulagQueue.size() == 1 ? gulagLocation1 : gulagLocation2);
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);

        if (gulagQueue.size() >= 2) {
            startGulag();
        }
    }

    private void startGulag() {
        gulagState = GulagState.ONGOING;
        Player player1 = gulagQueue.poll();
        Player player2 = gulagQueue.poll();

        // Teleport players to gulag arena and start the fight
        // Add logic to handle the fight and determine the winner
    }

    public void handleGulagWin(Player winner) {
        playerManager.setPlayerState(winner, PlayerState.RESURRECTED);
        winner.teleport(defaultRespawnLocation);
        giveGoldenArmorKit(winner);
        gulagState = GulagState.IDLE;
    }

    public void handleGulagLoss(Player loser) {
        playerManager.setPlayerState(loser, PlayerState.DEAD);
        loser.teleport(lobbyLocation);
        loser.getInventory().clear();
        loser.setHealth(20);
        loser.setFoodLevel(20);
        gulagState = GulagState.IDLE;
    }

    private void giveGoldenArmorKit(Player player) {
        ItemStack helmet = new ItemStack(Material.GOLDEN_HELMET);
        ItemStack chestplate = new ItemStack(Material.GOLDEN_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.GOLDEN_LEGGINGS);
        ItemStack boots = new ItemStack(Material.GOLDEN_BOOTS);

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public GulagState getGulagState() {
        return gulagState;
    }

    public int getEliminationYLevel() {
        return eliminationYLevel;
    }
}
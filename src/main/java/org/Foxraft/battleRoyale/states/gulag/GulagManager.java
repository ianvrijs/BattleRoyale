// GulagManager.java
package org.Foxraft.battleRoyale.states.gulag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.Foxraft.battleRoyale.listeners.PlayerMoveListener;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class GulagManager {
    private final PlayerManager playerManager;
    private final Location gulagLocation1;
    private final Location gulagLocation2;
    private final Location lobbyLocation;
    private final Location defaultRespawnLocation;
    private final Queue<Player> gulagQueue = new LinkedList<>();
    private GulagState gulagState = GulagState.IDLE;
    private final int eliminationYLevel;
    private final JavaPlugin plugin;
    private PlayerMoveListener playerMoveListener;
    private boolean countdownActive = false;

    public GulagManager(PlayerManager playerManager, JavaPlugin plugin) {
        this.playerManager = playerManager;
        this.plugin = plugin;
        this.gulagLocation1 = getLocationFromConfig(plugin, "gulag1", null);
        this.gulagLocation2 = getLocationFromConfig(plugin, "gulag2", null);
        this.lobbyLocation = getLocationFromConfig(plugin, "lobby", null);
        this.defaultRespawnLocation = new Location(Bukkit.getWorld("world"), 0, Objects.requireNonNull(Bukkit.getWorld("world")).getHighestBlockYAt(0, 0) + 2, 0);
        this.eliminationYLevel = plugin.getConfig().getInt("gulagHeight", 0); // Updated key
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
        if (playerManager.getPlayerState(player) == PlayerState.GULAG) {
            return; // Player is already in the Gulag
        }

        playerManager.setPlayerState(player, PlayerState.GULAG);
        playerManager.setEnteredGulag(player, true);
        gulagQueue.add(player);

        if (gulagQueue.size() == 1) {
            player.teleport(gulagLocation1);
        } else if (gulagQueue.size() == 2) {
            player.teleport(gulagLocation2);
            startGulagCountdown();
        } else {
            player.teleport(lobbyLocation);
            player.sendMessage(ChatColor.RED + "Sumo is full. Please wait for the next match.");
        }
        if (gulagQueue.size() == 1 || gulagQueue.size() == 2) {
            Player player1 = gulagQueue.peek();
            Player player2 = gulagQueue.size() > 1 ? ((LinkedList<Player>) gulagQueue).get(1) : null;
            playerMoveListener = new PlayerMoveListener(player1, player2, this);
            Bukkit.getPluginManager().registerEvents(playerMoveListener, plugin);
        }
    }

    private void startGulagCountdown() {
        countdownActive = true;
        Player player1 = gulagQueue.peek();
        Player player2 = gulagQueue.size() > 1 ? ((LinkedList<Player>) gulagQueue).get(1) : null;

        if (player1 != null && player2 != null) {
            Bukkit.broadcastMessage(ChatColor.GOLD + player1.getName() + ChatColor.GREEN +" will be fighting against " + ChatColor.GOLD +player2.getName() + ChatColor.GREEN+" in the Ring of Redemption!");

            new BukkitRunnable() {
                int countdown = 3;

                @Override
                public void run() {
                    if (countdown > 0) {
                        for (Player player : gulagQueue) {
                            player.sendTitle(ChatColor.YELLOW + "Sumo starting in", ChatColor.RED + String.valueOf(countdown), 10, 20, 10);
                        }
                        countdown--;
                    } else {
                        for (Player player : gulagQueue) {
                            player.sendTitle(ChatColor.RED + "Fight!", "", 10, 20, 10);
                            player.playSound(player.getLocation(), "minecraft:entity.ender_dragon.growl", 1.0f, 1.0f);
                        }
                        countdownActive = false;
                        startGulag();
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
        }
    }

    private void startGulag() {
        gulagState = GulagState.ONGOING;
        Player player1 = gulagQueue.poll();
        Player player2 = gulagQueue.poll();
    }

    public void handleGulagWin(Player winner) {
        if (winner == null) {
            return;
        }
        winner.sendMessage(ChatColor.GREEN + "You've redeemed yourself! You get one final chance.");
        Bukkit.broadcastMessage(ChatColor.GOLD + winner.getName() + ChatColor.GOLD + " has won the sumo! " + ChatColor.GOLD + "⚔");
        playerManager.setPlayerState(winner, PlayerState.RESURRECTED);

        // Teleport the winner immediately
        winner.teleport(defaultRespawnLocation);

        // Give the golden armor kit after a short delay
        new BukkitRunnable() {
            @Override
            public void run() {
                giveGoldenArmorKit(winner);
                gulagState = GulagState.IDLE;
                checkAndStartNewGulagMatch();
            }
        }.runTaskLater(plugin, 20L); // 1 second delay
    }

    public void handleGulagLoss(Player loser) {
        if (loser == null) {
            return;
        }
        //TODO fix this getting called twice
        playerManager.setPlayerState(loser, PlayerState.DEAD);
        new BukkitRunnable() {
            @Override
            public void run() {
                loser.sendMessage(ChatColor.RED + "You've been eliminated.");
                loser.teleport(lobbyLocation);
                loser.getInventory().clear();
                loser.setHealth(20);
                loser.setFoodLevel(20);
                gulagState = GulagState.IDLE;
                checkAndStartNewGulagMatch();
            }
        }.runTaskLater(plugin, 5L); //5 tick delay
    }

    public void unregisterPlayerMoveListener() {
        Bukkit.getLogger().info("Unregistering PlayerMoveListener");
        if (playerMoveListener != null) {
            PlayerMoveEvent.getHandlerList().unregister(playerMoveListener);
            playerMoveListener = null;
        }
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
    private void checkAndStartNewGulagMatch() {
        if (gulagQueue.size() >= 2) {
            startGulagCountdown();
        }
    }
    public boolean isCountdownActive() {
        return countdownActive;
    }

    public void handlePlayerLeave(Player player) {
        if (gulagQueue.contains(player)) {
            gulagQueue.remove(player);
            Player remainingPlayer = gulagQueue.poll();
            if (remainingPlayer != null) {
                handleGulagWin(remainingPlayer);
            }
            unregisterPlayerMoveListener();
        }
    }
}
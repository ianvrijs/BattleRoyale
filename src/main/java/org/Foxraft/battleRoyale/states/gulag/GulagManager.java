// GulagManager.java
package org.Foxraft.battleRoyale.states.gulag;

import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.Foxraft.battleRoyale.listeners.PlayerMoveListener;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GulagManager {
    private final PlayerManager playerManager;
    private Location gulagLocation1;
    private Location gulagLocation2;
    private Location lobbyLocation;
    private Location defaultRespawnLocation;
    private final Queue<Player> gulagQueue = new LinkedList<>();
    private final TeamManager teamManager;
    private GameManager gameManager;
    private GulagState gulagState = GulagState.IDLE;
    private final int eliminationYLevel;
    private final JavaPlugin plugin;
    private PlayerMoveListener playerMoveListener;
    private boolean countdownActive = false;

    public GulagManager(PlayerManager playerManager, JavaPlugin plugin, TeamManager teamManager) {
        this.playerManager = playerManager;
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.eliminationYLevel = plugin.getConfig().getInt("gulagHeight", 0);
        new BukkitRunnable() {
            @Override
            public void run() {
                loadLocations();
            }
        }.runTaskLater(plugin, 20L);
    }
    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    private Location getLocationFromConfig(JavaPlugin plugin, String path, Location defaultLocation) {
        if (!plugin.getConfig().contains(path + ".world") ||
                !plugin.getConfig().contains(path + ".x") ||
                !plugin.getConfig().contains(path + ".y") ||
                !plugin.getConfig().contains(path + ".z")) {
            plugin.getLogger().severe("Missing coordinates in config for path: " + path);
            return defaultLocation;
        }

        String worldName = plugin.getConfig().getString(path + ".world");
        if (worldName == null) {
            plugin.getLogger().severe("World name is missing in config for path: " + path);
            return defaultLocation;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().severe("Could not find world: " + worldName);
            return defaultLocation;
        }

        double x = plugin.getConfig().getDouble(path + ".x", 0);
        double y = plugin.getConfig().getDouble(path + ".y", 64);
        double z = plugin.getConfig().getDouble(path + ".z", 0);
        float yaw = (float) plugin.getConfig().getDouble(path + ".yaw", 0);
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch", 0);

        return new Location(world, x, y, z, yaw, pitch);
    }
    private void loadLocations() {
        Location loc1 = getLocationFromConfig(plugin, "gulag1", null);
        Location loc2 = getLocationFromConfig(plugin, "gulag2", null);
        Location lobby = getLocationFromConfig(plugin, "lobby", null);

        if (loc1 != null && loc2 != null && lobby != null) {
            this.gulagLocation1 = loc1;
            this.gulagLocation2 = loc2;
            this.lobbyLocation = lobby;

            World world = lobby.getWorld();
            assert world != null;
            this.defaultRespawnLocation = new Location(world, 0,
                    world.getHighestBlockYAt(0, 0) + 2, 0);

            plugin.getLogger().info("Successfully loaded locations: " +
                    "Lobby(" + lobby.getX() + "," + lobby.getY() + "," + lobby.getZ() + ") " +
                    "Gulag1(" + loc1.getX() + "," + loc1.getY() + "," + loc1.getZ() + ") " +
                    "Gulag2(" + loc2.getX() + "," + loc2.getY() + "," + loc2.getZ() + ")");
        } else {
            plugin.getLogger().severe("Failed to load one or more locations. Make sure world is loaded.");
            // Try again in 1 second
            new BukkitRunnable() {
                @Override
                public void run() {
                    loadLocations();
                }
            }.runTaskLater(plugin, 20L);
        }
    }

    public void enlistInGulag(Player player) {
        if (gulagLocation1 == null || gulagLocation2 == null) {
            plugin.getLogger().severe("Gulag locations are not set in config.yml");
            return;
        }
        plugin.getLogger().info("World name: " + gulagLocation1.getWorld().getName());
        World world = gulagLocation1.getWorld();
        if (world == null || !world.isChunkLoaded(world.getChunkAt(gulagLocation1))) {
            plugin.getLogger().severe("Gulag world is not loaded or chunk is not loaded");
            return;
        }
        if (playerManager.getPlayerState(player) == PlayerState.GULAG) {
            return;
        }
        if (gulagLocation1 == null || gulagLocation2 == null) {
            plugin.getLogger().severe("Gulag locations are not set in config.yml");
            return;
        }

        if (world == null) {
            plugin.getLogger().severe("Gulag world is not loaded");
            return;
        }
        gulagLocation1.setWorld(world);
        gulagLocation2.setWorld(world);

        // Don't enlist if player was already resurrected
        if (playerManager.getPlayerState(player) == PlayerState.RESURRECTED) {
            playerManager.setPlayerState(player, PlayerState.DEAD);
            player.teleport(lobbyLocation);
            return;
        }

        // Get game state from game manager
        GameState currentState = gameManager.getCurrentState();
        if (currentState != GameState.STORM) {
            playerManager.setPlayerState(player, PlayerState.DEAD);
            player.teleport(lobbyLocation);
            return;
        }

        // Count alive players
        int alivePlayers = 0;
        for (Team team : teamManager.getTeams().values()) {
            for (String playerName : team.getPlayers()) {
                Player p = Bukkit.getPlayer(playerName);
                if (p != null && (playerManager.getPlayerState(p) == PlayerState.ALIVE || playerManager.getPlayerState(p) == PlayerState.GULAG)) {
                    alivePlayers++;
                }
            }
        }

        // If 3 or fewer players are alive, don't enlist in gulag
        if (alivePlayers <= 3) {
            playerManager.setPlayerState(player, PlayerState.DEAD);
            player.teleport(lobbyLocation);
            return;
        }

        gulagQueue.add(player);
        if (gulagQueue.size() <= 2) {
            // Players in positions 1-2 are actively in gulag
            playerManager.setPlayerState(player, PlayerState.GULAG);
            playerManager.setEnteredGulag(player, true);
            player.teleport(gulagQueue.size() == 1 ? gulagLocation1 : gulagLocation2);

            if (gulagQueue.size() == 2) {
                startGulagCountdown();
            }
        } else {
            // Players in position 3+ are waiting
            player.teleport(lobbyLocation);
            player.sendMessage(ChatColor.RED + "Sumo is full. Please wait for the next match.");
        }

        // Set up move listener only for active gulag players
        if (gulagQueue.size() <= 2) {
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
            Bukkit.broadcastMessage(ChatColor.GOLD + player1.getName() + ChatColor.GREEN + " will be fighting against " + ChatColor.GOLD + player2.getName() + ChatColor.GREEN + " in the Ring of Redemption!");

            new BukkitRunnable() {
                int countdown = 5;

                @Override
                public void run() {
                    if (countdown > 0) {
                        player1.sendTitle(ChatColor.YELLOW + "Sumo starting in", ChatColor.RED + String.valueOf(countdown), 10, 20, 10);
                        player2.sendTitle(ChatColor.YELLOW + "Sumo starting in", ChatColor.RED + String.valueOf(countdown), 10, 20, 10);
                        countdown--;
                    } else {
                        player1.sendTitle(ChatColor.RED + "Fight!", "", 10, 20, 10);
                        player2.sendTitle(ChatColor.RED + "Fight!", "", 10, 20, 10);
                        player1.playSound(player1.getLocation(), "minecraft:entity.ender_dragon.growl", 1.0f, 1.0f);
                        player2.playSound(player2.getLocation(), "minecraft:entity.ender_dragon.growl", 1.0f, 1.0f);
                        countdownActive = false;
                        startGulag();
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
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
//        unregisterPlayerMoveListener();
        winner.sendMessage(ChatColor.GREEN + "You've redeemed yourself! You get one final chance.");
        Bukkit.broadcastMessage(ChatColor.GOLD + winner.getName() + ChatColor.GOLD + " has won the sumo! " + ChatColor.GOLD + "⚔");
        playerManager.setPlayerState(winner, PlayerState.RESURRECTED);
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndStartNewGulagMatch();
                giveGoldenArmorKit(winner);
                gulagState = GulagState.IDLE;
                winner.teleport(defaultRespawnLocation);
            }
        }.runTaskLater(plugin, 100L); // 5s
    }

    public void unregisterPlayerMoveListener() {
        if (playerMoveListener != null) {
            HandlerList.unregisterAll(playerMoveListener);
            playerMoveListener = null;
        }
    }
    private void giveGoldenArmorKit(Player player) {
        ItemStack helmet = new ItemStack(Material.GOLDEN_HELMET);
        ItemStack chestplate = new ItemStack(Material.GOLDEN_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.GOLDEN_LEGGINGS);
        ItemStack boots = new ItemStack(Material.GOLDEN_BOOTS);

        ItemMeta helmetMeta = helmet.getItemMeta();
        ItemMeta chestplateMeta = chestplate.getItemMeta();
        ItemMeta leggingsMeta = leggings.getItemMeta();
        ItemMeta bootsMeta = boots.getItemMeta();

        assert helmetMeta != null;
        helmetMeta.setDisplayName(ChatColor.GOLD + "Crown of Redemption");
        assert chestplateMeta != null;
        chestplateMeta.setDisplayName(ChatColor.GOLD + "Chestplate of Redemption");
        assert leggingsMeta != null;
        leggingsMeta.setDisplayName(ChatColor.GOLD + "Leggings of Redemption");
        assert bootsMeta != null;
        bootsMeta.setDisplayName(ChatColor.GOLD + "Boots of Redemption");

        List<String> lore = Arrays.asList(
                ChatColor.GRAY + "Forged in the Ring of Redemption by " + player.getName(),
                ChatColor.GRAY + "Worn by those who fought for their second chance"
        );

        helmetMeta.setLore(lore);
        chestplateMeta.setLore(lore);
        leggingsMeta.setLore(lore);
        bootsMeta.setLore(lore);

        helmet.setItemMeta(helmetMeta);
        chestplate.setItemMeta(chestplateMeta);
        leggings.setItemMeta(leggingsMeta);
        boots.setItemMeta(bootsMeta);

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
        unregisterPlayerMoveListener();
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
    public Plugin getPlugin() {
        return plugin;
    }
    public void clearGulag() {
        if (playerMoveListener != null) {
            unregisterPlayerMoveListener();
        }

        for (Player player : gulagQueue) {
            player.teleport(getLobbyLocation());
            playerManager.setPlayerState(player, PlayerState.DEAD);
            player.sendMessage(ChatColor.RED + "Sumo has been cancelled. You've been eliminated.");
        }
        //check for game end


        gulagQueue.clear();
        gulagState = GulagState.IDLE;
        countdownActive = false;
    }
    public void processGulagResult(Player winner, Player loser) {
        if (winner == null || !winner.isOnline() || loser == null || !loser.isOnline()) {
            gulagState = GulagState.IDLE;
            return;
        }

        if (playerMoveListener != null) {
            HandlerList.unregisterAll(playerMoveListener);
            playerMoveListener = null;
        }

        loser.teleport(lobbyLocation);
        playerManager.setPlayerState(loser, PlayerState.DEAD);
        loser.sendMessage(ChatColor.RED + "You've been eliminated from the Sumo!");
        loser.getInventory().clear();

        winner.sendMessage(ChatColor.GREEN + "You've redeemed yourself! You get one final chance.");
        Bukkit.broadcastMessage(ChatColor.GOLD + winner.getName() + ChatColor.GOLD + " has won the sumo! " + ChatColor.GOLD + "⚔");
        playerManager.setPlayerState(winner, PlayerState.RESURRECTED);

        final Player finalWinner = winner;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (finalWinner.isOnline()) {
                    finalWinner.teleport(defaultRespawnLocation);
                    giveGoldenArmorKit(finalWinner);
                    giveNoobieProtection(finalWinner);
                }
                gulagState = GulagState.IDLE;
                checkAndStartNewGulagMatch();
            }
        }.runTaskLater(plugin, 100L);
    }

    public List<Player> getGulagQueue() {
        return new LinkedList<>(gulagQueue);
    }
    private void giveNoobieProtection(Player player) {
        player.setInvulnerable(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.setInvulnerable(false);
                    player.sendMessage(ChatColor.RED + "Your noobie protection has worn off!");
                }
            }
        }.runTaskLater(plugin, 100L); // 5s
    }
}
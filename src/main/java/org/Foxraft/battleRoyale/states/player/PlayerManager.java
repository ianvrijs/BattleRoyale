package org.Foxraft.battleRoyale.states.player;

import org.Foxraft.battleRoyale.events.PlayerStateChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.Foxraft.battleRoyale.states.game.GameState;

import java.util.*;

/**
 * This class manages the state of players in the game.
 * Dependencies: PlayerState, Player, GameState, GulagManager
 */
public class PlayerManager {

    private final Map<Player, PlayerState> playerStates = new HashMap<>();
    private final Map<Player, Boolean> playerGulagStatus = new HashMap<>();
    private final Set<UUID> exemptedPlayers = new HashSet<>();

    public void setPlayerState(Player player, PlayerState state) {
        playerStates.put(player, state);
        Bukkit.getLogger().info("Setting player state for " + player.getName() + " to " + state);
        Bukkit.getPluginManager().callEvent(new PlayerStateChangeEvent(player, state));
        Bukkit.getLogger().info("Player " + player.getName() + " is now in state " + state);
    }
    public PlayerState getPlayerState(Player player) {
        return playerStates.getOrDefault(player, PlayerState.LOBBY);
    }

    public void removePlayer(Player player) {
        playerStates.remove(player);
        playerGulagStatus.remove(player);
    }

    public boolean isPlayerInState(Player player, PlayerState state) {
        return playerStates.getOrDefault(player, PlayerState.LOBBY) == state;
    }

    public boolean hasEnteredGulag(Player player) {
        return playerGulagStatus.getOrDefault(player, false);
    }

    public void setEnteredGulag(Player player, boolean entered) {
        playerGulagStatus.put(player, entered);
    }

    public void updatePlayerState(Player player, GameState gameState) {
        if (gameState == GameState.LOBBY) {
            setPlayerState(player, PlayerState.LOBBY);
            return;
        }

        PlayerState currentState = getPlayerState(player);

        if (currentState == PlayerState.GULAG || currentState == PlayerState.RESURRECTED) {
            return;
        }

        if (hasEnteredGulag(player)) {
            setPlayerState(player, PlayerState.GULAG);
        } else if (currentState == PlayerState.DEAD) {
            setPlayerState(player, currentState);
        } else {
            setPlayerState(player, PlayerState.ALIVE);
        }
    }
    public boolean isExempted(Player player) {
        return exemptedPlayers.contains(player.getUniqueId());
    }
    public void toggleExemption(Player player) {
        UUID uuid = player.getUniqueId();
        if (exemptedPlayers.contains(uuid)) {
            exemptedPlayers.remove(uuid);
        } else {
            exemptedPlayers.add(uuid);
        }
    }
}
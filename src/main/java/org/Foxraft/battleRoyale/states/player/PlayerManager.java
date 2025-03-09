package org.Foxraft.battleRoyale.states.player;

import org.bukkit.entity.Player;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private final Map<Player, PlayerState> playerStates = new HashMap<>();
    private final Map<Player, Boolean> playerGulagStatus = new HashMap<>();

    public void setPlayerState(Player player, PlayerState state) {
        playerStates.put(player, state);
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
        switch (gameState) {
            case LOBBY:
                setPlayerState(player, PlayerState.LOBBY);
                break;
            case GRACE:
            case STORM:
            case DEATHMATCH:
                if (hasEnteredGulag(player)) {
                    setPlayerState(player, PlayerState.GULAG);
                } else if (getPlayerState(player) == PlayerState.RESURRECTED) {
                    setPlayerState(player, PlayerState.RESURRECTED);
                } else if (getPlayerState(player) == PlayerState.DEAD) {
                    setPlayerState(player, PlayerState.DEAD);
                } else {
                    setPlayerState(player, PlayerState.ALIVE);
                }
                break;
        }
    }

    public void handlePlayerDeath(Player player, GameState gameState, GulagManager gulagManager) {
        if (gameState == GameState.STORM) {
            if (hasEnteredGulag(player)) {
                setPlayerState(player, PlayerState.DEAD);
                player.teleport(gulagManager.getLobbyLocation());
            } else {
                gulagManager.enlistInGulag(player);
            }
        } else {
            setPlayerState(player, PlayerState.DEAD);
            player.teleport(gulagManager.getLobbyLocation());
        }
    }
}
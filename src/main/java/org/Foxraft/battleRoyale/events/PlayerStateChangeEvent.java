package org.Foxraft.battleRoyale.events;

import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerStateChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final PlayerState newState;

    public PlayerStateChangeEvent(Player player, PlayerState newState) {
        this.player = player;
        this.newState = newState;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerState getNewState() {
        return newState;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
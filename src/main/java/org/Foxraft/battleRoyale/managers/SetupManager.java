package org.Foxraft.battleRoyale.managers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * This class is responsible for setting up the plugin configuration.
 * Depends on the JavaPlugin class.
 */
public class SetupManager {
    private final JavaPlugin plugin;

    public SetupManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setLobby(Player player) {
        Location location = player.getLocation();
        plugin.getConfig().set("lobby.world", Objects.requireNonNull(location.getWorld()).getName());
        plugin.getConfig().set("lobby.x", location.getX());
        plugin.getConfig().set("lobby.y", location.getY());
        plugin.getConfig().set("lobby.z", location.getZ());
        plugin.saveConfig();
        player.sendMessage(ChatColor.GREEN +"Lobby spawn point set to your current location.");
    }
    public void setStormSpeed(Player sender, float speed) {
        plugin.getConfig().set("stormSpeed", speed);
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN +"Storm speed set to " + speed + " blocks per second.");
    }

    public void setMapRadius(Player sender, int radius) {
        plugin.getConfig().set("mapRadius", radius);
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN +"Map radius set to " + radius + " blocks.");
    }
    public void setGulag(Player sender, int gulagNumber) {
        Location location = sender.getLocation();
        plugin.getConfig().set("gulag" + gulagNumber + ".world", location.getWorld().getName());
        plugin.getConfig().set("gulag" + gulagNumber + ".x", location.getX());
        plugin.getConfig().set("gulag" + gulagNumber + ".y", location.getY());
        plugin.getConfig().set("gulag" + gulagNumber + ".z", location.getZ());
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN +"Gulag " + gulagNumber + " spawn point set to your current location.");
    }

    public void setGraceTime(Player sender, int minutes) {
        plugin.getConfig().set("graceTime", minutes);
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN +"Grace time set to " + minutes + " minutes.");
    }

    public void setGulagHeight(Player sender, int height) {
        plugin.getConfig().set("gulagHeight", height);
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN +"Gulag height set to Y" + height);
    }
}

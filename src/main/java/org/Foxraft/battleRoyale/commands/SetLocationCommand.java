package org.Foxraft.battleRoyale.commands;

import org.Foxraft.battleRoyale.BattleRoyale;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Objects;

public class SetLocationCommand {
    private final BattleRoyale plugin;

    public SetLocationCommand(BattleRoyale plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String locationType) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return;
        }

        Location location = player.getLocation();
        String path = locationType.toLowerCase();

        plugin.getConfig().set(path + ".world", Objects.requireNonNull(location.getWorld()).getName());
        plugin.getConfig().set(path + ".x", Math.round(location.getX()));
        plugin.getConfig().set(path + ".y", Math.round(location.getY()));
        plugin.getConfig().set(path + ".z", Math.round(location.getZ()));
        plugin.getConfig().set(path + ".yaw", Math.round(location.getYaw()));
        plugin.getConfig().set(path + ".pitch", Math.round(location.getPitch()));
        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN + "Set " + locationType + " location to your current position.");
    }
}
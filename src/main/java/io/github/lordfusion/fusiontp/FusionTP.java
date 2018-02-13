package io.github.lordfusion.fusiontp;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * FusionTP is a teleportation plugin built for Minecraft 1.7.10 Bukkit servers.
 * Written by Lord_Fusion
 */
public final class FusionTP extends JavaPlugin
{
    public void onEnable()
    {
        getLogger().info("[FusionTP] What's up boys and girls, it's ya boi, FusionTP.");
        getServer().getPluginManager().registerEvents(new ListenerHandler(), this);
    }
    
    /**
     * Executes a given Fusion command.
     * @param sender - Source of the command
     * @param cmd - Command to be executed
     * @param label - Alias that was called
     * @param args - Passed command arguments
     * @return True if the command was successful, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        /* Force TP */
        if (cmd.getName().equalsIgnoreCase("fusiontp")) {
            if (args.length == 1) { // Teleport the sender to the specified player.
                // The command sender must be an online player.
                OfflinePlayer cmdSender = findPlayer(sender.getName());
                if (cmdSender == null || !cmdSender.isOnline()) {
                    sender.sendMessage("[FTP] Error: You are not an online player!");
                    return false;
                }
                Player origin = cmdSender.getPlayer();
                // Specified player must not be the sender.
                OfflinePlayer foundPlayer = findPlayer(args[0]);
                if (foundPlayer == null) {
                    sender.sendMessage("[FTP] Player '" + args[0] + "' was not found!");
                    return true;
                }
                if (origin.getName().equalsIgnoreCase(foundPlayer.getName())) {
                    sender.sendMessage("[FTP] You can't teleport to yourself!");
                    return false;
                }
                // Specified player must be online.
                if (!foundPlayer.isOnline()) {
                    sender.sendMessage("[FTP] '" + foundPlayer.getName() + "' is not online!");
                    return true;
                }
                Player destination = foundPlayer.getPlayer();
                // Teleport the sender to the specified player.
                if (onlineTeleport(origin, destination.getLocation())) {
                    sender.sendMessage("[FTP] Teleported to '" + destination.getName() + "'.");
                    return true;
                } else {
                    sender.sendMessage("[FTP] An error occurred during teleportation.");
                    return true;
                }
            } else if (args.length == 2) { // Teleport the first player to the second player.
                // Verify both parameters are online players.
                
                // If the second player is the sender, verify the sender has the TP Here permission.
                
                // Teleport the first player to the second player.
                // The origin must be an online player.
                OfflinePlayer foundOrigin = findPlayer(args[0]);
                if (foundOrigin == null) {
                    sender.sendMessage("[FTP] Player '" + args[0] + "' is not valid!");
                    return false;
                } else if (!foundOrigin.isOnline()) {
                    sender.sendMessage("[FTP] Player '" + foundOrigin.getName() + "' is not online!");
                    return true;
                }
                Player origin = foundOrigin.getPlayer();
                // The destination must be an online player.
                OfflinePlayer foundDestination = findPlayer(args[1]);
                if (foundDestination == null) {
                    sender.sendMessage("[FTP] Player '" + args[0] + "' is not valid!");
                    return false;
                } else if (!foundDestination.isOnline()) {
                    sender.sendMessage("[FTP] Player '" + foundDestination.getName() + "' is not online!");
                    return true;
                }
                Player destination = foundDestination.getPlayer();
                // If the destination is the sender, make sure they have the TPHere permission.
                if (destination.getName().equalsIgnoreCase(sender.getName()) && !sender.hasPermission("FusionTP.direct.here")) {
                    sender.sendMessage("[FTP] You aren't allowed to teleport players to you!");
                    return true;
                }
                // Teleport the origin to the destination.
                if (onlineTeleport(origin, destination.getLocation())) {
                    sender.sendMessage("[FTP] Teleported '" + origin.getName() + "' to '" + destination.getName() + "'.");
                    return true;
                } else {
                    sender.sendMessage("[FTP] An error occurred during teleportation.");
                    return true;
                }
            }
            return false;
        }
        /* Force TP Here */
        else if (cmd.getName().equalsIgnoreCase("fusiontphere")) {
            if (args.length == 1) {
                // The command sender must be an online player.
                OfflinePlayer cmdSender = findPlayer(sender.getName());
                if (cmdSender == null || !cmdSender.isOnline()) {
                    sender.sendMessage("[FTP] Error: You are not an online player!");
                    return false;
                }
                Player destination = cmdSender.getPlayer();
                // Specified player must not be the sender.
                OfflinePlayer foundPlayer = findPlayer(args[0]);
                if (foundPlayer == null) {
                    sender.sendMessage("[FTP] Player '" + args[0] + "' was not found!");
                    return true;
                }
                Player origin = foundPlayer.getPlayer();
                if (origin.getName().equalsIgnoreCase(sender.getName())) {
                    sender.sendMessage("[FTP] You can't teleport to yourself!");
                    return false;
                }
                // Specified player must be online.
                if (!foundPlayer.isOnline()) {
                    sender.sendMessage("[FTP] '" + foundPlayer.getName() + "' is not online!");
                    return true;
                }
                // Teleport the sender to the specified player.
                if (onlineTeleport(origin, destination.getLocation())) {
                    sender.sendMessage("[FTP] Teleported '" + origin.getName() + "' to you.");
                    return true;
                } else {
                    sender.sendMessage("[FTP] An error occurred during teleportation.");
                    return true;
                }
            }
            return false;
        }
        /* Request TP */
        else if (cmd.getName().equalsIgnoreCase("fusiontpa")) {
            
        }
        /* Request TP Here */
        else if (cmd.getName().equalsIgnoreCase("fusiontpahere")) {
        
        }
        /* Accept TP Request */
        else if (cmd.getName().equalsIgnoreCase("fusiontpaccept")) {
        
        }
        /* Deny TP Request */
        else if (cmd.getName().equalsIgnoreCase("fusiontpdeny")) {
        
        }
        return false;
    }
    
    /**
     * Find a player by their username.
     * @param username - Partial or full username to be found
     * @return A player matching the given search term, null if not found.
     */
    private OfflinePlayer findPlayer(String username)
    {
        for (Player onPlayer : getServer().getOnlinePlayers())
            if (onPlayer.getName().toLowerCase().contains(username.toLowerCase()))
                return onPlayer;
        for (OfflinePlayer offPlayer : getServer().getOfflinePlayers())
            if (offPlayer.getName().toLowerCase().contains(username.toLowerCase()))
                return offPlayer;
        return null;
    }
    
    /**
     * Find a player by their UUID.
     * @param uid - UUID to search by
     * @return A player matching the given UUID, null if not found.
     */
    private OfflinePlayer findPlayer(UUID uid)
    {
        Player onPlayer = getServer().getPlayer(uid);
        if (onPlayer == null) {
            return getServer().getOfflinePlayer(uid);
        } else
            return onPlayer;
    }
    
    /**
     * Teleport an online player.
     * @param sender Player to be teleported
     * @param destination New location for the player
     * @return True if the teleport is successful, false otherwise
     */
    private boolean onlineTeleport(Player sender, Location destination)
    {
        return(sender.teleport(destination));
    }
}

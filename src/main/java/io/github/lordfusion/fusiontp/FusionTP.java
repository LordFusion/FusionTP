package io.github.lordfusion.fusiontp;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * FusionTP is a teleportation plugin built for Minecraft 1.7.10 Bukkit servers.
 * Written by Lord_Fusion
 */
public final class FusionTP extends JavaPlugin
{
    static final String chatPrefix = ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + "FSN-TP" + ChatColor.GRAY + "] ";
    static final String consolePrefix = "[Fusion Teleport] ";
    
    static final Integer rtpDelay = 30;
    
    public void onEnable()
    {
        getLogger().info("What's up boys and girls, it's ya boi, FusionTP.");
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
            if (args.length == 1) // Teleport the sender to the specified player.
                return(fusionTeleport(sender, sender.getName(), args[0]));
            else if (args.length == 2) { // Teleport the first player to the second player.
                OfflinePlayer destinationPlayer = findPlayer(args[1]);
                if (destinationPlayer != null && destinationPlayer.getName().equalsIgnoreCase(sender.getName())) {
                    Permission perm = getServer().getPluginManager().getPermission("FusionTP.direct.here");
                    if (!sender.hasPermission(perm)) {
                        sender.sendMessage("[FSN-TP] Nice try! You don't have " + perm.toString());
                        return false;
                    }
                } else {
                    return(fusionTeleport(sender, args[0], args[1]));
                }
            }
            return false;
        }
        /* Force TP Here */
        else if (cmd.getName().equalsIgnoreCase("fusiontphere")) {
            if (args.length == 1)
                return(fusionTeleport(sender, args[0], sender.getName()));
            return false;
        }
        /* Force TP to Spawn */
        else if (cmd.getName().equalsIgnoreCase("fusionspawntp")) {
            if (args.length == 1)
                return(spawnTeleport(sender, args[0]));
            return false;
        }
        /* Random TP */
        else if (cmd.getName().equalsIgnoreCase("fusionrandomtp")) {
            if (args.length > 0)
                return false;
            return(randomTeleport(sender));
        }
        /* Fusion Test */
        else if (cmd.getName().equalsIgnoreCase("fusiontest")) {
            fusionTest(sender);
        }
        return false;
    }
    
    /* COMMANDS ******************************************************************************************** COMMANDS */
    
    /**
     * Teleport one player to another player.
     * Works for both online and offline players.
     * @param sender          Command sender, where command errors and messages will be sent
     * @param originName      Name of the player to be teleported
     * @param destinationName Name of the player who represents the teleport destination
     * @return True if the player was successfully teleported, false otherwise.
     * Todo: Make sure the return statement does its job
     */
    private boolean fusionTeleport(CommandSender sender, String originName, String destinationName)
    {
        // Verify the origin player
        OfflinePlayer originPlayer = findPlayer(originName);
        if (originPlayer == null) {
            sender.sendMessage("[FSN-TP] Player '" + originName + "' was not found!");
            return(true);
        }
        
        // Verify the destination player
        OfflinePlayer destinationPlayer = findPlayer(destinationName);
        if (destinationPlayer == null) {
            sender.sendMessage("[FSN-TP] Player '" + destinationName + "' was not found!");
            return(true);
        }
        
        if (originPlayer.isOnline()) { // Online TP
            Player onOriginPlayer = originPlayer.getPlayer();
            if (destinationPlayer.isOnline()) { // Online player to online player
                if (onlineTeleport(onOriginPlayer, destinationPlayer.getPlayer().getLocation())) {
                    sender.sendMessage("[FSN-TP] Teleported player '" + onOriginPlayer.getName() + "' to '" +
                        destinationPlayer.getName() + "'.");
                } else {
                    sender.sendMessage("[FSN-TP] Teleport failed!");
                }
                return(true);
            } else { // Online player to offline player
                PlayerHandler offlinePlayer = new PlayerHandler(PlayerHandler.findPlayerFile(destinationPlayer.getUniqueId()));
                Location destination = offlinePlayer.getPlayerLocation();
                
                if (onOriginPlayer.teleport(destination)) {
                    sender.sendMessage("[FSN-TP] Teleported player '" + onOriginPlayer.getName() + "' to last " +
                            "known location for '" + destinationPlayer.getName() + "'.");
                } else {
                    sender.sendMessage("[FSN-TP] Teleport failed!");
                }
                return(true);
            }
        } else { // Offline TP
            if (destinationPlayer.isOnline()) { // Offline player to online player
                Location destination = destinationPlayer.getPlayer().getLocation();
                if (offlineTeleport(originPlayer, destination)) {
                    sender.sendMessage("[FSN-TP] Offline-teleported player '" + originPlayer.getName() + "' to '" +
                    destinationPlayer.getName() + "'.");
                } else {
                    sender.sendMessage("[FSN-TP] Teleport failed!");
                }
                return true;
            } else { // Offline player to offline player
                PlayerHandler destPlayerData = new PlayerHandler(PlayerHandler.findPlayerFile(destinationPlayer.getUniqueId()));
                Location destination = destPlayerData.getPlayerLocation();
                if (offlineTeleport(originPlayer, destination)) {
                    sender.sendMessage("[FSN-TP] Offline-teleported player '" + originPlayer.getName() + "' to '" +
                            destinationPlayer.getName() + "'.");
                } else {
                    sender.sendMessage("[FSN-TP] Teleport failed!");
                }
                return true;
            }
        }
    }
    
    /**
     * Teleport a player to the spawn point in the main World
     * @param sender     Command sender, where command errors and messages will be sent
     * @param playerName Name of the player to be teleported
     * @return True if the player was successfully teleported, false otherwise.
     * Todo: Make sure the return statement does its job
     */
    private boolean spawnTeleport(CommandSender sender, String playerName)
    {
        OfflinePlayer player = findPlayer(playerName);
        if (player == null) {
            sender.sendMessage("[FSN-TP] Player '" + player.getName() + "' was not found!");
            return true;
        }
        
        boolean success;
        if (player.isOnline()) {
            success = player.getPlayer().teleport(WorldHandler.getServerSpawn());
        } else {
            PlayerHandler playerData = new PlayerHandler(PlayerHandler.findPlayerFile(player.getUniqueId()));
            success = playerData.setPlayerLocation(WorldHandler.getServerSpawn());
        }
        if (success)
            sender.sendMessage("[FSN-TP] '" + playerName + "' was successfully teleported to spawn.");
        return true;
    }
    
    private boolean randomTeleport(CommandSender sender)
    {
        // Verify the command sender is a valid online player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only online players can run this command!");
            return true;
        }
        Player player = ((Player) sender).getPlayer();
        
        // Check if the command is off of cooldown
        if (player.hasMetadata("FSN.RTP.LAST")) {
            MetadataValue mdValue = player.getMetadata("FSN.RTP.LAST").get(0);
            Instant lastCalled = Instant.parse(mdValue.asString());
            Instant nextCallAvailable = lastCalled.plus(rtpDelay, ChronoUnit.SECONDS);
            if (nextCallAvailable.isAfter(Instant.now())) {
                sender.sendMessage(chatPrefix + ChatColor.RED + "You must wait " + Instant.now()
                        .until(nextCallAvailable, ChronoUnit.SECONDS) + " seconds before using RTP again!");
                sendConsoleInfo("Player attempted to use RTP, but they are on cooldown: "
                        + ((Player) sender).getName());
                return true;
            }
        }
        
        // Find a random location for the world the player is in
        WorldHandler playerWorld = new WorldHandler(player.getWorld());
        Location randomLocation = playerWorld.getRandomTpDestination();
        
        // Teleport the player
        if (onlineTeleport(player, randomLocation)) {
            player.setMetadata("FSN.RTP.LAST", new LazyMetadataValue(this, new Callable<Object>()
            {
                final Instant time = Instant.now();
                public Instant call() throws Exception
                {
                    return(time);
                }
            }));
            sender.sendMessage( chatPrefix + ChatColor.LIGHT_PURPLE + "You have been teleported to a random location!");
            sendConsoleInfo("Player was random-teleported: " + ((Player) sender).getName());
        } else {
            sender.sendMessage(chatPrefix + ChatColor.RED + "There was an error teleporting you. Try again later.");
            sendConsoleWarn("Random-teleport FAILED for: " + ((Player) sender).getName());
        }
        return true;
    }
    
    /**
     * I use this method to test things. You should never expect any form of real documentation around these parts.
     * @param sender Command sender
     */
    private void fusionTest(CommandSender sender)
    {
        World world = WorldHandler.findWorld(0);
        UUID worldId = world.getUID();
        sender.sendMessage(String.valueOf(worldId.getLeastSignificantBits()));
        sender.sendMessage(String.valueOf(worldId.getMostSignificantBits()));
    }
    
    /* METHODS ********************************************************************************************** METHODS */
    
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
    
    /**
     * Set a new location for an offline player.
     * @param player Player to be teleported
     * @param destination New location for the player
     * @return True if the teleport is successful, false otherwise
     */
    private boolean offlineTeleport(OfflinePlayer player, Location destination)
    {
        PlayerHandler offlinePlayer = new PlayerHandler(PlayerHandler.findPlayerFile(player.getUniqueId()));
        return(offlinePlayer.setPlayerLocation(destination));
    }
    
    /* STATIC METHODS ******************************************************************************** STATIC METHODS */
    
    /**
     * Sends a message to the server console, with the Info priority level.
     * @param message Message for console
     */
    void sendConsoleInfo(String message)
    {
        getLogger().info(message);
    }
    
    /**
     * Sends a message to the server console, with the Warning priority level.
     * @param message Message for console
     */
    void sendConsoleWarn(String message)
    {
        getLogger().warning(message);
    }
}

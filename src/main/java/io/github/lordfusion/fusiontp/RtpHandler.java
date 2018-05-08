package io.github.lordfusion.fusiontp;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.LazyMetadataValue;

import java.time.Instant;
import java.util.concurrent.Callable;

public class RtpHandler implements Runnable
{
    private FusionTP plugin;
    private Player player;
    
    RtpHandler(FusionTP plugin, Player player)
    {
        this.plugin = plugin;
        this.player = player;
    }
    
    @Override
    public void run()
    {
        // Find a random location for the world the player is in
        WorldHandler playerWorld = new WorldHandler(player.getWorld());
        Location randomLocation = playerWorld.getRandomTpDestination();
        
        // Make sure the location exists.
        if (randomLocation == null) {
            player.sendMessage(FusionTP.chatPrefix + ChatColor.LIGHT_PURPLE + "A safe location was not found.");
            FusionTP.sendConsoleWarn("Random-teleport FAILED for: " + player.getName());
            return;
        }
        
        // Teleport the player
        if (this.plugin.onlineTeleport(player, randomLocation)) {
            player.setMetadata("FSN.RTP.LAST", new LazyMetadataValue(plugin, new Callable<Object>()
            {
                final Instant time = Instant.now();
                public Instant call() throws Exception
                {
                    return(time);
                }
            }));
            player.sendMessage(FusionTP.chatPrefix + ChatColor.LIGHT_PURPLE + "You have been teleported to a random location!");
            FusionTP.sendConsoleInfo("Player was random-teleported: " + player.getName() + " -> (" +
                    randomLocation.getX() + ", " + randomLocation.getY() + ", " + randomLocation.getZ() + ") in world "
                    + randomLocation.getWorld().getName());
        } else {
            player.sendMessage(FusionTP.chatPrefix + ChatColor.RED + "There was an error teleporting you. Try again later.");
            FusionTP.sendConsoleWarn("Random-teleport FAILED for: " + player.getName());
        }
    }
}

package io.github.lordfusion.fusiontp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.MetadataValue;

public final class ListenerHandler implements Listener
{
    ListenerHandler()
    {
        Bukkit.getLogger().info("[FusionTP] Listener loaded.");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        boolean mdValueRefined;
        Player player = event.getPlayer();
        if (player.getMetadata("FSN.NOTP").isEmpty()) {
            return;
        } else {
            MetadataValue mdValueRaw = player.getMetadata("FSN.NOTP").get(0);
            mdValueRefined = mdValueRaw.asBoolean();
        }
        
        if (mdValueRefined) {
            event.setCancelled(true);
        }
    }
}

package io.github.lordfusion.fusiontp.commands;

import io.github.lordfusion.fusiontp.DataManager;
import io.github.lordfusion.fusiontp.FusionTP;
import io.github.lordfusion.fusiontp.utilities.RtpHandler;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RandomTeleport implements CommandExecutor
{
    private TextComponent ERROR_CONSOLE, ERROR_ON_COOLDOWN, ERROR_INTERNAL, ERROR_WORLD_DISALLOWED;
    
    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length != 0)
            return false;
        if (!(sender instanceof Player)) {
            FusionTP.sendUserMessage(sender, ERROR_CONSOLE);
            return true;
        }
        
        // Doing this just to make variable names easier to understand
        Player player = ((Player) sender).getPlayer();
        if (player == null) {
            FusionTP.sendUserMessage(sender, ERROR_INTERNAL);
            FusionTP.sendConsoleWarn("Player-sender was null for RTP!");
            return true;
        }
        
        // Check for cooldown
        DataManager dataManager = FusionTP.getInstance().getDataManager();
        if (dataManager.isOnRtpCooldown(player)) {
            FusionTP.sendUserMessage(sender, ERROR_ON_COOLDOWN);
            FusionTP.sendConsoleInfo("Player attempted to use RTP, but is on cooldown: " + player.getName());
            return true;
        }
        
        // Check for world rtp blacklist
        String worldName = player.getWorld().getName();
        if ((!dataManager.doWorldWhitelist() && Arrays.asList(dataManager.getWorldList()).contains(worldName)) || // Blacklist
                (dataManager.doWorldWhitelist() && !Arrays.asList(dataManager.getWorldList()).contains(worldName))) { // Whitelist
            FusionTP.sendUserMessage(sender, ERROR_WORLD_DISALLOWED);
            FusionTP.sendConsoleInfo("RTP blocked for disallowed world: " + worldName);
            return true;
        }
        
        // This is now the RTP Handler's problem.
        RtpHandler rtp = new RtpHandler(player);
        rtp.run();
        return true;
    }
}

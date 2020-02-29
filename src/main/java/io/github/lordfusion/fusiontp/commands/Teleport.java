package io.github.lordfusion.fusiontp.commands;

import io.github.lordfusion.fusiontp.DataManager;
import io.github.lordfusion.fusiontp.FusionTP;
import io.github.lordfusion.fusiontp.utilities.Teleporter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Simple teleportation from one player to another.
 */
public class Teleport implements CommandExecutor
{
    private static TextComponent ERROR_CONSOLE, ERROR_PERMISSION, ERROR_INVALID_TARGET, ERROR_INVALID_SOURCE, ERROR_FAILURE,
            SUCCESS_COMMANDSENDER, SUCCESS_SOURCE;
    
    public Teleport()
    {
        this.setupMessages();
    }
    
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
        if (args.length > 2 || args.length < 1) {
            return false;
        } else if (args.length == 1 && !(sender instanceof Player)) {
            FusionTP.sendUserMessage(sender, ERROR_CONSOLE);
            return true;
        }
    
        OfflinePlayer source; // Player to be teleported
        OfflinePlayer target; // Destination player
        
        if (args.length == 1) { // Teleport the sender to the specified player
            if (!sender.hasPermission("fusion.tp.direct.to")) {
                TextComponent msg = (TextComponent)ERROR_PERMISSION.duplicate();
                msg.addExtra("fusion.tp.direct.to");
                FusionTP.sendUserMessage(sender, msg);
                FusionTP.sendConsoleInfo("Failed to teleport; sender missing permission.");
                return true;
            }
            target = DataManager.findPlayer(args[0]);
            if (target == null) {
                TextComponent msg = (TextComponent)ERROR_INVALID_TARGET.duplicate();
                msg.addExtra('\"' + args[0] + '\"');
                FusionTP.sendUserMessage(sender, msg);
                FusionTP.sendConsoleInfo("Failed to teleport; invalid target.");
                return true;
            }
            source = (Player)sender;
        } else { // Teleport args[0] to args[1]
            source = DataManager.findPlayer(args[0]);
            if (source == null) {
                TextComponent msg = (TextComponent) ERROR_INVALID_SOURCE.duplicate();
                msg.addExtra('\"' + args[0] + '\"');
                FusionTP.sendUserMessage(sender, msg);
                FusionTP.sendConsoleInfo("Failed to teleport; invalid source.");
                return true;
            }
            target = DataManager.findPlayer(args[1]);
            if (target == null) {
                TextComponent msg = (TextComponent)ERROR_INVALID_TARGET.duplicate();
                msg.addExtra('\"' + args[0] + '\"');
                FusionTP.sendUserMessage(sender, msg);
                FusionTP.sendConsoleInfo("Failed to teleport; invalid target.");
                return true;
            }
            if (!sender.hasPermission("fusion.tp.direct.here")) {
                TextComponent msg = (TextComponent)ERROR_PERMISSION.duplicate();
                msg.addExtra("fusion.tp.direct.here");
                FusionTP.sendUserMessage(sender, msg);
                FusionTP.sendConsoleInfo("Failed to teleport; sender missing permission.");
                return true;
            }
        }
        
        if (Teleporter.teleport(source, target)) {
            if (source.equals(sender)) {
                FusionTP.sendUserMessage(sender, SUCCESS_SOURCE);
            } else {
                if (source.isOnline())
                    FusionTP.sendUserMessage((CommandSender)source, SUCCESS_SOURCE);
                FusionTP.sendUserMessage(sender, SUCCESS_COMMANDSENDER);
            }
        } else {
            FusionTP.sendUserMessage(sender, ERROR_FAILURE);
        }
        return true;
    }
    
    private void setupMessages()
    {
        ERROR_CONSOLE = new TextComponent("Console cannot use this command with a single argument.");
        ERROR_CONSOLE.setColor(ChatColor.DARK_RED);
        
        ERROR_PERMISSION = new TextComponent("Missing permission: ");
        ERROR_PERMISSION.setColor(ChatColor.RED);
        
        ERROR_INVALID_TARGET = new TextComponent("Invalid target: ");
        ERROR_INVALID_TARGET.setColor(ChatColor.RED);
        
        ERROR_INVALID_SOURCE = new TextComponent("Invalid source: ");
        ERROR_INVALID_SOURCE.setColor(ChatColor.RED);
        
        ERROR_FAILURE = new TextComponent("Teleport failed.");
        ERROR_FAILURE.setColor(ChatColor.RED);
        
        SUCCESS_COMMANDSENDER = new TextComponent("Teleported!");
        SUCCESS_COMMANDSENDER.setColor(ChatColor.GREEN);
        
        SUCCESS_SOURCE = new TextComponent("You were teleported.");
        SUCCESS_SOURCE.setColor(ChatColor.GREEN);
    }
}

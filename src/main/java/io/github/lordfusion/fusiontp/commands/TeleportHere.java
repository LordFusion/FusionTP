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

public class TeleportHere implements CommandExecutor
{
    private static TextComponent ERROR_CONSOLE, ERROR_INVALID_SOURCE, ERROR_FAILURE,
            SUCCESS_TARGET, SUCCESS_COMMANDSENDER;
    
    public TeleportHere()
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
        if (args.length != 1)
            return false;
        if (!(sender instanceof Player)) {
            FusionTP.sendUserMessage(sender, ERROR_CONSOLE);
            return true;
        }
    
        OfflinePlayer source = DataManager.findPlayer(args[0]);
        OfflinePlayer target = ((Player)sender).getPlayer();
        
        if (source == null) {
            TextComponent msg = (TextComponent) ERROR_INVALID_SOURCE.duplicate();
            msg.addExtra('\"' + args[0] + '\"');
            FusionTP.sendUserMessage(sender, msg);
            FusionTP.sendConsoleInfo("Failed to teleport; invalid source.");
            return true;
        }
    
        if (Teleporter.teleport(source, target)) {
            FusionTP.sendUserMessage(sender, SUCCESS_COMMANDSENDER);
            if (source.isOnline())
                FusionTP.sendUserMessage((CommandSender)source, SUCCESS_TARGET);
        } else {
            FusionTP.sendUserMessage(sender, ERROR_FAILURE);
        }
        return true;
    }
    
    private void setupMessages()
    {
        ERROR_CONSOLE = new TextComponent("Console cannot use this command.");
        ERROR_CONSOLE.setColor(ChatColor.DARK_RED);
        
        ERROR_INVALID_SOURCE = new TextComponent("Invalid player: ");
        ERROR_INVALID_SOURCE.setColor(ChatColor.RED);
        
        ERROR_FAILURE = new TextComponent("Teleport failed.");
        ERROR_FAILURE.setColor(ChatColor.RED);
        
        SUCCESS_COMMANDSENDER = new TextComponent("Teleported!");
        SUCCESS_COMMANDSENDER.setColor(ChatColor.GREEN);
        
        SUCCESS_TARGET = new TextComponent("You were teleported.");
        SUCCESS_TARGET.setColor(ChatColor.GREEN);
    }
}

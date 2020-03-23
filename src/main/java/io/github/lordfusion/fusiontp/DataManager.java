package io.github.lordfusion.fusiontp;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.bukkit.Bukkit.getServer;

public class DataManager
{
    private static final String CONFIG_FILENAME = "/config.yml";
    private String dataFolderPath;
    private YamlConfiguration config;
    
    // Config option names
    protected static final String RTP_DELAY = "RTP Delay";
    private static final String WORLD_WHITELIST = "RTP World Whitelist";
    private static final String WORLD_LIST = "RTP Worlds List";
    
    // Config defaults
    private static final HashMap<String, Object> CONFIG_DEFAULTS = new HashMap<String, Object>() {{
        put(RTP_DELAY, 0);
        put(WORLD_WHITELIST, false);
        put(WORLD_LIST, new ArrayList<String>());
    }};
    
    // Integrations
    private boolean essentialsEnabled;
    private Plugin essentials;
    
    /**
     * The manager for all FusionTP's data needs.
     * @param pluginDataFolder Folder where plugin data is to be stored
     */
    DataManager(String pluginDataFolder)
    {
        // Configurations
        this.dataFolderPath = pluginDataFolder;
        this.loadConfigFile();
        
        // Integrations
        this.essentialsEnabled = checkEssentialsIntegration();
    }
    
    /**
     * Loads config.yml from file.
     * If it does not exist, a new one will be generated.
     * If an option is not present, it will be set to its default value.
     */
    private void loadConfigFile()
    {
        File configFile = new File(this.dataFolderPath + CONFIG_FILENAME);
        if (configFile.exists())
            this.config = YamlConfiguration.loadConfiguration(configFile);
        else
            resetConfigFile();
        
        // Verify file completion
        Set<String> configKeys = config.getKeys(false);
        boolean fileChanged = false;
        if (!configKeys.contains(RTP_DELAY)) {
            config.set(RTP_DELAY, CONFIG_DEFAULTS.get(RTP_DELAY));
            fileChanged = true;
        }
        if (!configKeys.contains(WORLD_WHITELIST)) {
            config.set(WORLD_WHITELIST, CONFIG_DEFAULTS.get(WORLD_WHITELIST));
            fileChanged = true;
        }
        if (!configKeys.contains(WORLD_LIST)) {
            this.setWorldArrayList((List<String>)CONFIG_DEFAULTS.get(WORLD_LIST));
            fileChanged = true;
        }
        
        if (fileChanged)
            saveConfigFile();
    }
    
    /**
     * Completely replace the current config file with a fresh new one, filled with default values.
     */
    private void resetConfigFile()
    {
        // Completely reset the config.
        this.config = new YamlConfiguration();
        // Header
        this.config.options().header("Fusion TP Configuration File");
        this.config.options().indent(2);
        // Default Values
        this.config.set(RTP_DELAY, CONFIG_DEFAULTS.get(RTP_DELAY));
        this.config.set(WORLD_WHITELIST, CONFIG_DEFAULTS.get(WORLD_WHITELIST));
        this.setWorldArrayList((List<String>)CONFIG_DEFAULTS.get(WORLD_LIST));
        FusionTP.sendConsoleInfo("Default config restored.");
        // Save
        this.saveConfigFile();
    }
    
    /**
     * Attempt to save the config file.
     * If save fails, sends a warning message to Console.
     */
    private void saveConfigFile()
    {
        try {
            this.config.save(dataFolderPath + CONFIG_FILENAME);
        } catch (IOException e) {
            FusionTP.sendConsoleWarn("FAILED to save config file:");
            e.printStackTrace();
            return;
        }
        FusionTP.sendConsoleInfo("Config saved to file.");
    }
    
    // Config Editing ******************************************************************************** Config Editing //
    public int getRtpDelay()
    {
        if (this.config != null && this.config.contains(RTP_DELAY))
            return this.config.getInt(RTP_DELAY, (int)CONFIG_DEFAULTS.get(RTP_DELAY));
        return 0;
    }
    public void setRtpDelay(int i)
    {
        this.config.set(RTP_DELAY, i);
    }
    public boolean doWorldWhitelist()
    {
        if (this.config != null && this.config.contains(WORLD_WHITELIST))
            return this.config.getBoolean(WORLD_WHITELIST, (boolean)CONFIG_DEFAULTS.get(WORLD_WHITELIST));
        return false;
    }
    public void setWorldWhitelist(int i)
    {
        this.config.set(WORLD_WHITELIST, i);
    }
    private ArrayList<String> getWorldArrayList()
    {
        if (this.config != null && this.config.contains(WORLD_LIST)) {
            return (ArrayList<String>) this.config.getStringList(WORLD_LIST);
        }
        return (ArrayList<String>) CONFIG_DEFAULTS.get(WORLD_LIST);
    }
    public String[] getWorldList()
    {
        if (this.config != null && this.config.contains(WORLD_LIST)) {
            return this.getWorldArrayList().toArray(new String[0]);
        }
        return new String[0];
    }
    private void setWorldArrayList(List<String> list)
    {
        this.config.set(WORLD_LIST, list);
    }
    public void addToWorldList(String worldName)
    {
        ArrayList<String> worldList = this.getWorldArrayList();
        worldList.add(worldName);
        this.setWorldArrayList(worldList);
    }
    public boolean removeFromWorldList(String worldName)
    {
        ArrayList<String> worldList = this.getWorldArrayList();
        if (!worldList.contains(worldName))
            return false;
        
        worldList.remove(worldName);
        this.setWorldArrayList(worldList);
        return true;
    }
    
    // Metadata ******************************************************************************************** Metadata //
    /**
     * Uses metadata to determine whether the player is currently waiting on an RTP cooldown.
     * @param player Player attempting to RTP.
     * @return True if they are on cooldown and need to wait. False if ready to teleport.
     */
    public boolean isOnRtpCooldown(Player player)
    {
        return false;
    }
    
    /**
     * Uses metadata to set the player onto a cooldown for the RTP command.
     * Cooldown length determined by the DataManager.
     * @param player Player who was RTPd.
     */
    public void putOnRtpCooldown(Player player)
    {
    
    }
    
    // Integrations ************************************************************************************ Integrations //
    private boolean checkEssentialsIntegration()
    {
        FusionTP.sendConsoleInfo("Checking for Essentials integration...");
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            FusionTP.sendConsoleInfo("Essentials integration found!");
            this.essentials = Bukkit.getPluginManager().getPlugin("Essentials");
            return true;
        } else {
            FusionTP.sendConsoleWarn("No Essentials integrations found.");
            this.essentials = null;
            return false;
        }
    }
    public Essentials getEssentials()
    {
        return (Essentials)this.essentials;
    }
    
    // Info-Seeking ************************************************************************************ Info-Seeking //
    /**
     * Find a player by their username.
     * @param username - Partial or full username to be found
     * @return A player matching the given search term, null if not found.
     */
    public static OfflinePlayer findPlayer(String username)
    {
        for (Player onPlayer : getServer().getOnlinePlayers())
            if (onPlayer.getName().toLowerCase().contains(username.toLowerCase()))
                return onPlayer;
        for (OfflinePlayer offPlayer : getServer().getOfflinePlayers())
            if (offPlayer.getName().toLowerCase().contains(username.toLowerCase()))
                return offPlayer;
        return null;
    }
}

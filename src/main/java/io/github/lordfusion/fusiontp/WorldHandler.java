package io.github.lordfusion.fusiontp;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Deals with world interactions, with or without Multiverse-Core.
 * All static methods, all the time!
 *
 * ToDo: Look into World.getUID()
 */
public class WorldHandler
{
    private NbtCompound fileContents;
    private File fileLocation;
    
    /**
     * Creates a specialized NbtManager to handle level.dat files
     * @param levelDatFile A valid level.dat file
     */
    public WorldHandler(File levelDatFile)
    {
        try {
            this.fileContents = NbtManager.loadFile(levelDatFile);
        } catch (IOException exc) {
            Bukkit.getServer().getLogger().warning("[Fusion TP] ERROR: FAILED TO READ PLAYER DAT FILE.");
        }
        this.fileLocation = levelDatFile;
    }
    
    /* NON-STATIC METHODS ************************************************************************ NON-STATIC METHODS */
    /**
     * Reads the DAT information to find the dimension number.
     * @return int - Dimension number
     */
    public int getDimNumber()
    {
        NbtCompound worldData = fileContents.getCompound("Data");
        int dimNumber = worldData.getInteger("dimension");
        Bukkit.getServer().getLogger().info("Dimension number: " + dimNumber);
        return(dimNumber);
    }
    
    /**
     * Reads the DAT information to find the world name.
     * @return String - World name
     */
    public String getWorldName()
    {
        NbtCompound worldData = fileContents.getCompound("Data");
        return(worldData.getString("LevelName"));
    }
    
    /* STATIC METHODS ******************************************************************************** STATIC METHODS */
    /**
     * Returns the main overworld's player spawn point.
     * @return Location of the main overworld's player spawn point
     */
    public static Location getServerSpawn()
    {
        World mainWorld = Bukkit.getServer().getWorld("world");
        return(mainWorld.getSpawnLocation());
    }
    
    /**
     * Find the desired world, using standard Bukkit implementation.
     * Only use this if Multiverse is not available!
     * @param dimensionNumber Short dimension number used by player DAT files
     * @return World - Desired world, if found. NULL otherwise
     */
    static World findWorld(int dimensionNumber)
    {
        File[] allWorldFiles = Bukkit.getWorldContainer().listFiles();
        if (allWorldFiles == null) {
            Bukkit.getServer().getLogger().warning("[FSN-TP] Failed to find files in the World folder!");
            return(null);
        }
        for (File file : allWorldFiles) {
            if (file.isDirectory()) {
                File levelDat = new File(file, "level.dat");
                if (levelDat.exists()) {
                    WorldHandler world = new WorldHandler(levelDat);
                    if (world.getDimNumber() == dimensionNumber) {
                        return(Bukkit.getWorld(world.getWorldName()));
                        
                        // Believed to have caused issues, such as generating overworld chunks in the End
//                        String worldName = world.getWorldName();
//                        Bukkit.getServer().createWorld(new WorldCreator(worldName)); // Loads the world if needed
//                        return(Bukkit.getServer().getWorld(worldName));
                    }
                }
            }
        }
        
        return(Bukkit.getWorld("world")); //Todo: Find a better implementation of this method.
    }
    
    /**
     * Find the desired world by its friendly name
     * @param worldName Name of the world
     * @return World - Desired world, if found. NULL otherwise
     */
    static World findWorld(String worldName)
    {
        // Check for online worlds first
        World foundWorld = Bukkit.getServer().getWorld(worldName);
        if (foundWorld != null)
            return foundWorld;
        // Couldn't find it, check through all worlds we can find
        File[] allWorldFiles = Bukkit.getWorldContainer().listFiles();
        if (allWorldFiles == null) {
            Bukkit.getServer().getLogger().warning("[FSN-TP] Failed to find files in the World folder!");
            return(null);
        }
        for (File file : allWorldFiles) {
            if (file.isDirectory()) {
                File levelDat = new File(file, "level.dat");
                if (levelDat.exists()) {
                    WorldHandler world = new WorldHandler(levelDat);
                    if (world.getWorldName().equalsIgnoreCase(worldName)) {
                        Bukkit.getServer().createWorld(new WorldCreator(worldName));
                        return(Bukkit.getServer().getWorld(worldName));
                    }
                }
            }
        }
        return(null);
    }
}

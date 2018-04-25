package io.github.lordfusion.fusiontp;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Reads and edits player DAT files
 */
public class PlayerHandler
{
    private NbtCompound fileContents;
    private File fileLocation;
    
    /**
     * Create a PlayerHandler using a given player DAT file
     * @param playerDatFile An existing [player uuid].dat file
     */
    public PlayerHandler(File playerDatFile)
    {
        try {
            this.fileContents = NbtManager.loadFile(playerDatFile);
        } catch (IOException exc) {
            Bukkit.getServer().getLogger().warning("[FSN-TP] ERROR: FAILED TO READ PLAYER DAT FILE.");
        }
        this.fileLocation = playerDatFile;
    }
    
    /* NON-STATIC METHODS ************************************************************************ NON-STATIC METHODS */
    /**
     * Read the player's last-updated location from the file
     * @return Player's last location
     */
    public Location getPlayerLocation()
    {
        int playerDimension = fileContents.getInteger("Dimension");
        World playerWorld = WorldHandler.findWorld(playerDimension);
        
        NbtList<Double> playerPosition = fileContents.getList("Pos");
        double x= playerPosition.getValue(0);
        double y= playerPosition.getValue(1);
        double z= playerPosition.getValue(2);
        
        return new Location(playerWorld,x,y,z);
    }
    
    /**
     * Change a player's location via their data file.
     * Only use on offline players! This will get overwritten when the player logs off!
     * @param loc   New player location
     * @return      True if the file was successfully edited, false otherwise
     */
    public boolean setPlayerLocation(Location loc)
    {
        File worldFolder = loc.getWorld().getWorldFolder();
        WorldHandler world = new WorldHandler(new File (worldFolder,"level.dat"));
        int worldId = world.getDimNumber();
        fileContents.put("Dimension", worldId);
        
        double x= loc.getX();
        double y= loc.getY();
        double z= loc.getZ();
        NbtList<Double> newLoc = NbtFactory.ofList("Pos",x,y,z);
        fileContents.put(newLoc);
    
        fileContents.put("WorldUUIDLeast", loc.getWorld().getUID().getLeastSignificantBits());
        fileContents.put("WorldUUIDMost", loc.getWorld().getUID().getMostSignificantBits());
        
        return(NbtManager.saveFile(fileContents, fileLocation));
    }

    /* STATIC METHODS ******************************************************************************** STATIC METHODS */
    
    /**
     * Find the specified player's DAT file from the server World folder
     * @param playerUuid Player's unique ID in standard format (ex: a53eb2f2-0885-4d40-a51e-4edc3dc915cf)
     * @return A DAT file representing a player if found,
     */
    public static File findPlayerFile(UUID playerUuid)
    {
        // Check main World PlayerData folder
        File playerFolder = new File(Bukkit.getWorldContainer().getAbsolutePath(), "playerdata");
        if (!playerFolder.exists()) {
            Bukkit.getServer().getLogger().info("Failed to get PlayerData folder.");
        } else {
            for (File playerFile : playerFolder.listFiles()) {
                if (playerFile.getName().contains(playerUuid.toString()))
                    return playerFile;
            }
        }
        Bukkit.getServer().getLogger().warning("Failed to find player file for '" + playerUuid + "'.");
        return null;
    }
}

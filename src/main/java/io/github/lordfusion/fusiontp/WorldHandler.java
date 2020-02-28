package io.github.lordfusion.fusiontp;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;


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
    private World world;
    
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
    
    public WorldHandler(World bukkitWorld)
    {
        File levelDatFile = findWorldFile(bukkitWorld);
        try {
            this.fileContents = NbtManager.loadFile(levelDatFile);
        } catch (IOException exc) {
            FusionTP.sendConsoleWarn("ERROR: FAILED TO READ WORLD DAT FILE.");
        }
        this.fileLocation = levelDatFile;
        this.world = bukkitWorld;
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
        //Bukkit.getServer().getLogger().info("Dimension number: " + dimNumber);
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
    
    /**
     * Get the current world border from the WorldBorder plugin.
     * @return World border data
     */
    private BorderData getWorldBorder()
    {
        if (!isWorldBorderEnabled())
            return null;
        Plugin wbPlugin = Bukkit.getServer().getPluginManager().getPlugin("WorldBorder");
        WorldBorder worldBorder = (WorldBorder)wbPlugin;
        BorderData borderData = worldBorder.getWorldBorder(this.getWorldName());
        return(borderData);
    }
    
    /**
     * Find a random location for a player to teleport to within the given world.
     * Obeys world borders, if they exist from the WorldBorder plugin.
     * Spawns players on the top block at the location. If that block is water or lava, another location is selected.
     * @return Safe randomTP location.
     */
    Location getRandomTpDestination()
    {
        BorderData worldBorder = this.getWorldBorder();
        
        int maxX, maxZ, minX, minZ;
        int randX = 0, randZ = 0, topY = 256, tries = 0;
        while (randX == 0 && randZ == 0 && tries < 10) {
            // Generate random x- and z-coordinates.
            if (worldBorder != null) {
                int centerX = (int) worldBorder.getX();
                int centerZ = (int) worldBorder.getZ();
                int radiusX = worldBorder.getRadiusX();
                int radiusZ = worldBorder.getRadiusZ();
                maxX = centerX + radiusX;
                maxZ = centerZ + radiusZ;
                minX = centerX - radiusX;
                minZ = centerZ - radiusZ;
    
                while (randX == 0 && randZ == 0) {
                    randX = ThreadLocalRandom.current().nextInt(minX, maxX);
                    randZ = ThreadLocalRandom.current().nextInt(minZ, maxZ);
                    if (!worldBorder.insideBorder(randX, randZ)) {
                        randX = 0;
                        randZ = 0;
                    }
        
                }
            } else {
                maxX = 20000;
                maxZ = 20000;
                minX = -20000;
                minZ = -20000;
                // Todo: Config options
        
                randX = ThreadLocalRandom.current().nextInt(minX, maxX);
                randZ = ThreadLocalRandom.current().nextInt(minZ, maxZ);
            }
            
            // Find a safe y-coordinate, if possible.
            Block topBlock = null;
            if (world.getEnvironment().equals(World.Environment.NETHER)) { // Nether world
                for (int i=1; i<= 124; i++) { // Check every block for a suitable landing location
                    Location landingLocation = new Location(this.world, randX, i, randZ);
                    if (checkSafeLocation(landingLocation)) {
                        topBlock = world.getBlockAt(randX, i, randZ);
                        break;
                    }
                }
            } else if (world.getEnvironment().equals(World.Environment.THE_END)) { // End world
                return null;
            } else { // Normal world
                Block highestBlock = world.getHighestBlockAt(randX, randZ);
                Location landingLocation = new Location(this.world, randX, highestBlock.getY()-1, randZ);
                if (checkSafeLocation(landingLocation))
                    topBlock = world.getBlockAt(randX, highestBlock.getY()-1, randZ);
            }
            
            // If a safe location wasn't found at the given x- and z- coordinates, try again.
            if (topBlock == null) {
                randX = 0;
                randZ = 0;
            } else {
                topY = topBlock.getY();
            }
            tries++;
        }
        
        if (randX == 0 && randZ == 0)
            return null;
        return new Location(world, randX, topY+1, randZ);
    }
    
    /**
     * Make sure the location is safe for a player to spawn.
     * Safe locations must not be a fluid, and must have 2 empty blocks for the player.
     * @param location Location to check.
     * @return True if the location is safe, false otherwise.
     */
    private boolean checkSafeLocation(Location location)
    {
        // Todo: Add a config option to block problematic modded blocks (such as BiomesOP Ash)
        Block bot = world.getBlockAt(location);
        if (bot.isLiquid() || bot.isEmpty()) {
            return false;
        }
        Block mid = world.getBlockAt(location.add(0,1,0));
        if (mid.getType().isSolid() || mid.isLiquid()
                || mid.getType().toString().equalsIgnoreCase("BIOMESOPLENTY")
                || mid.getType().toString().contains("netherore")) {
            return false;
        }
        Block top = world.getBlockAt(location.add(0,1,0));
        if (top.getType().isSolid() || top.isLiquid()
                || top.getType().toString().equalsIgnoreCase("BIOMESOPLENTY")
                || mid.getType().toString().contains("netherore")) {
            return false;
        }
        
        // Todo: Check block above top block for sand, gravel, etc.
        
        return true;
    }
    
    /* STATIC METHODS ******************************************************************************** STATIC METHODS */
    /**
     * Returns the main overworld's player spawn point.
     * @return Location of the main overworld's player spawn point
     */
    static Location getServerSpawn()
    {
        World mainWorld = Bukkit.getServer().getWorld("world");
        return(mainWorld.getSpawnLocation());
    }
    
    static File findWorldFile(World world)
    {
        File worldFolder = world.getWorldFolder();
        return(new File(worldFolder, "level.dat"));
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
    
    /**
     * Checks the server for the plugin "WorldBorder"
     * @return True if enabled, false otherwise
     */
    private static boolean isWorldBorderEnabled()
    {
        return(Bukkit.getServer().getPluginManager().isPluginEnabled("WorldBorder"));
    }
}

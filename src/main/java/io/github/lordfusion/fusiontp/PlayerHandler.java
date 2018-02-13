package io.github.lordfusion.fusiontp;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;

import java.io.File;
import java.io.IOException;

/**
 * DAT files are the spawn of Satan, with no regard given to non-Mojang parties that may wish to interact with them.
 * That being said, here's where I pretend like I know what I'm doing.
 */
public class PlayerHandler
{
    private NbtCompound fileContents;
    
    /**
     * Loads a player's DAT file into the handler for reading and editing.
     * @param playerDatFile A valid DAT file pulled from the server
     */
    public PlayerHandler(File playerDatFile)
    {
        try {
            this.fileContents = loadFile(playerDatFile);
        } catch (IOException exc) {
            System.out.println("[Fusion TP] ERROR: FAILED TO READ PLAYER DAT FILE.");
        }
    }
    
    private static NbtCompound loadFile(File datFile) throws IOException
    {
        
        return(null);
    }
}

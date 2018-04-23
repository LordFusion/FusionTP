package io.github.lordfusion.fusiontp;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * DAT files are the spawn of Satan, with no regard given to non-Mojang parties that may wish to interact with them.
 * That being said, here's where I pretend like I know what I'm doing.
 */
final class NbtManager
{
    /**
     * Loads the raw DAT file into a manageable NBT Compound.
     * @param datFile A valid player DAT file
     * @return An NbtCompound representing the data contained in the given file
     *          NULL if the given file is not valid
     * @throws IOException Error in reading the given file
     */
    static NbtCompound loadFile(File datFile) throws IOException
    {
        if (datFile.exists()) {
            FileInputStream fileStream = new FileInputStream(datFile);
            GZIPInputStream compressedStream = new GZIPInputStream(fileStream);
            DataInputStream inputStream = new DataInputStream(compressedStream);
            NbtCompound output = NbtBinarySerializer.DEFAULT.deserializeCompound(inputStream);
            inputStream.close();
            if (output != null)
                return(output);
        }
        return(null);
    }
    /**
     * Save the NBT compound to overwrite the player's DAT file.
     * @return  True if saving the file doesn't throw an error.
     *          False if it does.
     */
    static boolean saveFile(NbtCompound nbt, File fileLocation)
    {
        try {
            FileOutputStream fileStream = new FileOutputStream(fileLocation);
            GZIPOutputStream compressedStream = new GZIPOutputStream(fileStream);
            DataOutputStream outputStream = new DataOutputStream(compressedStream);
            
            NbtBinarySerializer.DEFAULT.serialize(nbt, outputStream);
            outputStream.close();
            
            return true;
        } catch (IOException exc) {
            Bukkit.getServer().getLogger().warning("Failed to save player DAT file.");
            Bukkit.getServer().getLogger().info(exc.toString());
            return false;
        }
    }
}
package io.github.lordfusion.fusiontp;

import org.bukkit.plugin.java.JavaPlugin;

public final class FusionTP extends JavaPlugin
{
    public void onEnable()
    {
        getLogger().info("[FusionTP] What's up boys and girls, it's ya boi, FusionTP.");
        getServer().getPluginManager().registerEvents(new ListenerHandler(), this);
    }
}

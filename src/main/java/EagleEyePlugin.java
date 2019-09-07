import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Main class called by Bukkit/Spigot/Paper to initialize
 * plugin
 */
public class EagleEyePlugin extends JavaPlugin {

    private String pluginName;
    public String prefix;

    /**
     * Defines a message prefix, prepares necessary
     * files/folders, commands and registers events
     * from Backup Events when plugin has
     * successfully initialized
     */
    @Override
    public void onEnable() {
        pluginName = getDescription().getName();
        // Announcement prefix
        prefix = String.format("%s[%s%s%s]%s ",
                ChatColor.WHITE,
                ChatColor.BLUE,
                this.getDescription().getName(),
                ChatColor.WHITE,
                ChatColor.RESET
        );

        // State plugin is active
        Bukkit.getLogger().info(prefix + ChatColor.GREEN + "Activated");

        // Setup config file
        this.setupConfigFile();

        // Register event triggers
        this.getServer().getPluginManager().registerEvents(new EagleEyeEvents(this), this);

    }

    /**
     * Announces deactivation when plugin is disabled
     */
    @Override
    public void onDisable() {
        Bukkit.getLogger().info(prefix + ChatColor.RED + "Deactivated");
    }

    /**
     * Returns the file which contains the plugin
     * @return File containing plugin
     */
    File getPluginFile() { return this.getFile(); }

    private void setupConfigFile() {

        this.createFolder();
        getConfig().options().copyDefaults(true);
        getConfig().options().header("Caching is used to store usernames and UUIDs (1 week by default)");
        getConfig().addDefault("Caching.enabled", true);
        getConfig().addDefault("Caching.expiryInMinutes", 604800);
        getConfig().addDefault("AutoUpdate.enabled", true);
        saveConfig();

    }

    private void createFolder() {
        // Create folder for data
        if (!getDataFolder().exists()) {
            Bukkit.getLogger().info(prefix + ChatColor.RED + pluginName + " folder does not exist");
            if (getDataFolder().mkdir())
                Bukkit.getLogger().info(prefix + ChatColor.GREEN + pluginName + " folder has been created");
            else
                Bukkit.getLogger().info(prefix + ChatColor.RED + "Failed to create " + pluginName + " folder");
        }
    }

}

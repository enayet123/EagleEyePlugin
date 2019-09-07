import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Manages config file of all protected blocks
 */
class ProtectedConfig {

    private EagleEyePlugin plugin;
    private FileConfiguration conf;
    private File file;

    /**
     * Constructor used to create a config or initialize a
     * yaml file while utilising caching from MojangAPI class
     * @param plugin Plugin used to get prefix and plugin name
     * @param guarded Empty HashMap for loading values from yaml
     * @param api Caching class which interacts with Mojang API
     */
    ProtectedConfig(EagleEyePlugin plugin, HashMap<Block, Owners> guarded, MojangAPI api) {
        this.plugin = plugin;

        // Create folder if not exists
        if (!plugin.getDataFolder().exists())
            if (!plugin.getDataFolder().mkdir()) {
                Bukkit.getLogger().info(plugin.prefix + ChatColor.RED + "Failed to create folder " +
                        plugin.getDescription().getName());
                Bukkit.getLogger().info(plugin.prefix + ChatColor.RED + "Shutting down plugin...");
                Bukkit.getPluginManager().disablePlugin(plugin); // Disable plugin
                return;
            }

        // Create file if not exists
        file = new File(plugin.getDataFolder(), "protected.yml");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) throw new IOException("Failed to create file");
                Bukkit.getLogger().info(plugin.prefix + ChatColor.GREEN + "Created new protected.yml");
            } catch (IOException e) {
                Bukkit.getLogger().info(plugin.prefix + ChatColor.RED + "Failed to create protected.yml");
                Bukkit.getLogger().info(plugin.prefix + ChatColor.RED + "Shutting down plugin...");
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(plugin); // Disable plugin
                return;
            }
        }

        // Initialize config with existing content
        this.init(guarded, api);

    }

    /**
     * Puts a list of owners to a block in yaml file. This
     * method is also used to remove single users by over-
     * writing existing Owners.
     * @param owners List of owner(s)
     * @param block Block owners are assigned to
     */
    void put(Owners owners, Block block) {
        conf.set(toXYZ(block), owners.asList());
        this.save();
    }

    /**
     * Used to remove the last/only owner of a block
     * @param block Block owner is assigned to
     */
    void remove(Block block) {
        conf.set(toXYZ(block), null);
        this.save();
    }

    private void init(HashMap<Block, Owners> guarded, MojangAPI api) {

        // Load file content
        conf = YamlConfiguration.loadConfiguration(file);

        // For every protected block
        for (String uuid: conf.getKeys(false)) {
            // Get block
            Block block = fromXYZ(uuid);
            // Get list of owners
            List<String> owners = (List<String>) conf.getList(uuid);
            // Add every owner to guard list
            Owners o = new Owners(api);
            for (String owner: owners)
                o.add(UUID.fromString(owner));
            guarded.put(block, o);
        }

    }

    private void save() {
        try {
            conf.save(file); // Save current conf data to yaml file
        } catch (IOException e) {
            Bukkit.getLogger().info(plugin.prefix + ChatColor.RED + "Failed to update protected.yml");
            e.printStackTrace();
        }
    }

    private String toXYZ(Block b) {
        // Converts a block to a colon delimited string with world, X, Y and Z locations
        return String.format("%s:%s:%s:%s", b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
    }

    private Block fromXYZ(String xyz) {
        // Parse String to Block
        String[] loc = xyz.split(":");
        return Bukkit.getWorld(loc[0]).getBlockAt(
                (int) Float.parseFloat(loc[1]),
                (int) Float.parseFloat(loc[2]),
                (int) Float.parseFloat(loc[3])
        );
    }

}

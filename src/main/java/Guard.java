import me.kbrewster.exceptions.APIException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Deals with all protection related interactions
 */
public class Guard {

    private EagleEyePlugin plugin;
    private GuardMap guarded;
    private MojangAPI api;

    /**
     * Constructor used to prepare guard
     * @param plugin Main plugin
     */
    Guard(EagleEyePlugin plugin) {
        this.plugin = plugin;
        this.api = new MojangAPI(plugin);
        this.guarded = new GuardMap(plugin, api);
    }

    /**
     * Asynchronously adds unprotected block to guard
     * @param player Player placing wall sign
     * @param username Username of owner to add
     * @param block Block to assign owner to
     */
    void guardNew(Player player, String username, Block block) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Verify user
                UUID uuid = api.getUUID(username);

                // Add first owner
                Owners o = new Owners(uuid, api);
                guarded.put(block, o);
                player.sendMessage(plugin.prefix + ChatColor.GREEN + "Now protected by " + ChatColor.YELLOW + o);

            } catch (IOException | APIException e) {
                player.sendMessage(plugin.prefix + ChatColor.RED + "Failed to put " + ChatColor.YELLOW + username);
            }
        });

    }

    /**
     * Asynchronously adds/removes owner from block
     * @param player Player placing wall sign
     * @param username Username of owner
     * @param block Block to assign or un-assign owner to or from
     */
    void guardExisting(Player player, String username, Block block) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Verify third party user
                UUID playerOfInterest = api.getUUID(username);

                // If player exists in owners
                if (guarded.get(block).contains(playerOfInterest)) {

                    // Player of interest is the current player or the player is an Op
                    if (playerOfInterest.equals(player.getUniqueId()) || player.isOp())
                        remove(player, block); // Remove player

                } else {

                    // Player wants to put another owner
                    Owners o = guarded.get(block);

                    // Check if player of interest is online or player is Op
                    if (Bukkit.getServer().getPlayer(playerOfInterest) != null || player.isOp()) {
                        o.add(playerOfInterest);
                        guarded.put(block, o);
                        player.sendMessage(plugin.prefix + ChatColor.GREEN + "Now protected by " + ChatColor.YELLOW + o);
                    } else {
                        player.sendMessage(plugin.prefix + ChatColor.YELLOW + username + ChatColor.RED + " must be online");
                    }

                }

            } catch (IOException | APIException e) {
                player.sendMessage(plugin.prefix + ChatColor.RED + "Failed to put " + ChatColor.YELLOW + username);
            }
        });

    }

    private void remove(Player player, Block block) {
        Owners owners;

        // If there are multiple owners
        if ((owners = guarded.get(block)).hasMulti()) {

            // Remove the one
            owners.remove(player.getUniqueId());
            guarded.put(block, owners);
            player.sendMessage(plugin.prefix + ChatColor.GREEN + "Now only protected by " + ChatColor.YELLOW + owners);

        } else { // Single owner

            // Remove block from list
            guarded.remove(block);
            String type = block.getType().name().toLowerCase();
            player.sendMessage(plugin.prefix + ChatColor.RED + "This " + type + " is no longer protected");

        }

    }

    /**
     * Deal with non-Op user attempting to take ownership
     * of an already assigned block
     * @param player Player placing wall sign
     * @param block Block player is attempting to manipulate
     */
    void notifyUnavailable(Player player, Block block) {
        Owners o = guarded.get(block);
        player.sendMessage(plugin.prefix + ChatColor.RED + "Block is protected by " + ChatColor.YELLOW + o);
    }

    /**
     * Returns status of block in relation to player
     * @param player Player requesting status
     * @param block Block to get status of
     * @return Enum representing availability
     */
    GuardStatus statusOfBlock(Player player, Block block) {
        Owners owners;

        // If block owned by a player
        if ((owners = guarded.get(block)) != null) {
            // If current player is an owner or is Op
            if (owners.contains(player.getUniqueId()) || player.isOp())
                return GuardStatus.GUARDED;
            else
                return GuardStatus.UNAVAILABLE;
        }

        // No one owns block
        return GuardStatus.AVAILABLE;

    }

    /**
     * Deals with players manipulating inventories
     * @param block Block containing inventory
     * @param e Event triggered by player
     */
    void handleInventoryClickEvent(Block block, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        // Only cancel if guard deems necessary
        if (statusOfBlock(player, block) == GuardStatus.UNAVAILABLE) {
            String t = block.getType().name().toLowerCase(); // Block type
            Owners o = guarded.get(block); // Owners

            // Cancel event and kick player from inventory view
            e.setCancelled(true);
            player.closeInventory();

            // Send player message when Mojang API replies
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                player.sendMessage(plugin.prefix + ChatColor.RED + "This " + t + " belongs to " + ChatColor.YELLOW + o);
            });
        }

    }

    /**
     * Deals with players breaking blocks with inventories
     * @param e Event triggered by player breaking block
     */
    void handleBlockBreakEvent(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        // Only cancel if guard deemed necessary
        if (statusOfBlock(player, block) == GuardStatus.UNAVAILABLE) {
            String t = block.getType().name().toLowerCase(); // Block type
            Owners o = guarded.get(block); // Owners

            // Cancel event
            e.setCancelled(true);

            // Send player message when Mojang API replies
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                player.sendMessage(plugin.prefix + ChatColor.RED + "This " + t + " belongs to " + ChatColor.YELLOW + o);
            });
        } else {
            // TODO: Allow config to decide if owners can break their block?
            // Block is being broken and therefore unprotected
            guarded.remove(e.getBlock());
            // Send player a message
            String t = block.getType().name().toLowerCase(); // Block type
            player.sendMessage(plugin.prefix + ChatColor.RED + "This " + t + " is no longer protected");
        }

    }

    /**
     * Gets cached Mojang API
     * @return Mojang API instance
     */
    MojangAPI getApi() { return api; }

}

/**
 * Intermediary class used to handle manipulation of
 * data in memory and in yaml file
 */
class GuardMap {

    private HashMap<Block, Owners> guarded = new HashMap<Block, Owners>();
    private ProtectedConfig conf;

    /**
     * Constructor creates yaml file instance
     * @param plugin Main plugin
     * @param api API used when initially loading UUIDs
     *            from yaml
     */
    GuardMap(EagleEyePlugin plugin, MojangAPI api) {
        this.conf = new ProtectedConfig(plugin, guarded, api);
    }

    /**
     * Puts key (Block) with value (Owners) into HashMap
     * @param block Block to assign owners to
     * @param owners New or modified list of owners
     */
    void put(Block block, Owners owners) {
        guarded.put(block, owners);
        conf.put(owners, block);
    }

    /**
     * Removes key value pair from list completely
     * @param block Block to make available
     */
    void remove(Block block) {
        guarded.remove(block);
        conf.remove(block);
    }

    /**
     * Gets associated value from Map
     * @param block Block of interest
     * @return Associated owners
     */
    Owners get(Block block) { return guarded.get(block); }

}

/**
 * Handles a set of owners based on UUID
 */
class Owners {

    HashSet<UUID> uuids = new HashSet<>();
    MojangAPI api;

    /**
     * Constructor used to create instance with initial owner
     * @param uuid UUID of initial owner
     * @param api Cached Mojang API
     */
    Owners(UUID uuid, MojangAPI api) { this.uuids.add(uuid); this.api = api; }

    /**
     * Constructor used to create empty owner set
     * @param api Cached Mojang API
     */
    Owners(MojangAPI api) { this.api = api; }

    /**
     * Adds an additional owner
     * @param uuid New additional owner
     */
    void add(UUID uuid) { uuids.add(uuid); }

    /**
     * Removes existing owner
     * @param uuid Owner to remove
     */
    void remove(UUID uuid) { uuids.remove(uuid); }

    /**
     * Verifies if UUID is one of existing owners
     * @param uuid UUID of player to check
     * @return Is player an existing owner
     */
    boolean contains(UUID uuid) { return uuids.contains(uuid); }

    /**
     * Checks if there are multiple owners
     * @return Is there more than one owner
     */
    boolean hasMulti() { return uuids.size() > 1; }

    /**
     * Gets owner UUID's as a List of Strings
     * @return List of UUID's mapped to Strings
     */
    List<String> asList() { return uuids.stream().map(UUID::toString).collect(Collectors.toList()); }

    /**
     * Uses Mojang API to convert UUID to Username but returns
     * the name "Someone" on failure
     * @return Username or default to "Someone"
     */
    @Override
    public String toString() {
        return uuids
            .stream()
            .map(x -> {
                try {
                    return api.getName(x);
                } catch (IOException | APIException e) {
                    return "Someone";
                }
            })
            .collect(Collectors.joining(", "));
    }

}

enum GuardStatus {
    AVAILABLE, // No one owns block
    UNAVAILABLE, // Another player owns the block
    GUARDED, // Current player owns the block
}
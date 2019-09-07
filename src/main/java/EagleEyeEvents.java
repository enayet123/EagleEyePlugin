import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import types.InventoryMaterials;
import types.Signs;

/**
 * Listens for events that are related to protecting
 * or unprotecting blocks.
 */
public class EagleEyeEvents implements Listener {

    private EagleEyePlugin plugin;
    private Guard guard;
    private boolean updateQueued = false;

    /**
     * Initializes event listener class
     * @param plugin Refers back to the main class, EagleEye
     */
    public EagleEyeEvents(EagleEyePlugin plugin) {
        this.plugin = plugin;
        this.guard = new Guard(this.plugin);
    }

    /**
     * This event triggers when a player joins a server
     * @param e The event related to a player joining
     */
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {

        // Cache username and UUID
        guard.getApi().cache(e.getPlayer().getName(), e.getPlayer().getUniqueId());

        // Check for plugin update
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new UpdaterRunnable(plugin, e.getPlayer(), this));

    }

    /**
     * Adds metadata to wall signs if block types are valid
     * @param e Event triggered by placing a block
     */
    @EventHandler
    public void onWallSignPlaceEvent(BlockPlaceEvent e) {
        Player p = e.getPlayer(); // Player that triggered event

        // If Player is placing a WALL_SIGN on a protectable item
        if (Signs.contains(e.getBlock().getType()) && InventoryMaterials.contains(e.getBlockAgainst().getType()))
            // Set temporary metadata and wait for SignChangeEvent
            e.getBlock().setMetadata("protect", new ProtectableMeta(p, e.getBlockAgainst()));

    }

    /**
     * Checks if sign had metadata placed on it, if so
     * validate sign text and put protection
     * @param e Event triggered when wall sign 'Done'
     *          button is pressed
     */
    @EventHandler
    public void onSignChangeEvent(SignChangeEvent e) {
        Player p = e.getPlayer();
        Sign sign = (Sign) e.getBlock().getState();

        // Validate sign syntax and check for metadata
        if (Signs.hasValidSyntax(e) && Signs.hasMeta(sign)) {

            // Get target block
            Block target = Signs.getBlockFromMeta(sign);
            // Validate it is still a guardable block
            if (InventoryMaterials.contains(target.getType())) {

                // Get username off sign
                String username = Signs.getUsername(e.getLines());

                // Deal with each scenario of ownership
                switch(guard.statusOfBlock(p, target)) {
                    case AVAILABLE: // Block has no owner
                        if (username.equalsIgnoreCase(p.getName()) || p.isOp())
                            guard.guardNew(p, username, target);
                        else
                            p.sendMessage(plugin.prefix + ChatColor.RED + "You must be op to protect for others");
                        break;
                    case GUARDED: // Block belongs to current player or player is Op
                        guard.guardExisting(p, Signs.getUsername(e.getLines()), target);
                        break;
                    case UNAVAILABLE: // Block belongs to another player
                        guard.notifyUnavailable(p, target);
                        break;
                }

                // Break sign
                sign.getBlock().breakNaturally();

            }

        }

    }

    /**
     * Enforces ownership check before items can be removed from
     * the inventory.
     * @param e Triggered when a player clicks on any item while on
     *          the inventory view
     */
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {

        // Only continue is Player triggered event
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        // Inventories
        Inventory i = e.getInventory();
        InventoryHolder h = i.getHolder();

        // Inventory types
        if (h instanceof Chest) {
            guard.handleInventoryClickEvent(((Chest) h).getBlock(), e);
        } else if (h instanceof DoubleChest) {
            guard.handleInventoryClickEvent(((Chest) ((DoubleChest) h).getLeftSide()).getBlock(), e);
            guard.handleInventoryClickEvent(((Chest) ((DoubleChest) h).getRightSide()).getBlock(), e);
        } else if (h instanceof Furnace) {
            guard.handleInventoryClickEvent(((Furnace) h).getBlock(), e);
        } else if (h instanceof Hopper) {
            guard.handleInventoryClickEvent(((Hopper) h).getBlock(), e);
        } else if (h instanceof Dispenser) {
            guard.handleInventoryClickEvent(((Dispenser) h).getBlock(), e);
        } else if (h instanceof Barrel) {
            guard.handleInventoryClickEvent(((Barrel) h).getBlock(), e);
        } else if (h instanceof Dropper) {
            guard.handleInventoryClickEvent(((Dropper) h).getBlock(), e);
        /*} else if (i instanceof StonecutterInventory) {
            Block b = Bukkit.getWorld(i.getLocation().getWorld().getName()).getBlockAt(i.getLocation()); // Doesn't work
            guard.handleInventoryClickEvent(p, b, e);*/
        } else if (i instanceof LoomInventory) {
            Block b = Bukkit.getWorld(i.getLocation().getWorld().getName()).getBlockAt(i.getLocation());
            guard.handleInventoryClickEvent(b, e);
        /*} else if (i instanceof CartographyInventory) {
            Block b = Bukkit.getWorld(i.getLocation().getWorld().getName()).getBlockAt(i.getLocation()); // Doesn't work
            guard.handleInventoryClickEvent(p, b, e);*/
        } else if (i instanceof GrindstoneInventory) {
            Block b = Bukkit.getWorld(i.getLocation().getWorld().getName()).getBlockAt(i.getLocation());
            guard.handleInventoryClickEvent(b, e);
        }

        // TODO: Add doors, pressure plates, buttons?

    }

    /**
     * Enforces ownership check before a block can be broken
     * by a player.
     * @param e Triggered when a player attempts to break a
     *          block
     */
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        guard.handleBlockBreakEvent(e);
    }

    // TODO: Deal with explosions

    /**
     * Returns if an update is queued
     * @return is update queued
     */
    boolean isUpdateQueued() { return updateQueued; }

    /**
     * Sets updateQueued to true, this can only be set to false through a reload/restart
     */
    void setUpdateQueued() { updateQueued = true; }

}

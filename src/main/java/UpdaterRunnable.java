import net.gravitydevelopment.updater.Updater;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Checks for plugin update from bukkit.org
 * Auto updates can backupEvents disabled from the config.yml file
 */
public class UpdaterRunnable implements Runnable {

    private EagleEyePlugin plugin;
    private Player player;
    private EagleEyeEvents ipEvents;
    private final String msg;

    /**
     * Initializes the runnable with the information required to
     * run a successful update
     * @param plugin Plugin to get prefix of a message/Use on updater
     * @param player Player triggering the event
     * @param ipEvents Reference back to calling class used to
     *                     update boolean value (Prevent multi downloads)
     */
    public UpdaterRunnable(EagleEyePlugin plugin, Player player, EagleEyeEvents ipEvents) {
        this.plugin = plugin;
        this.player = player;
        this.ipEvents = ipEvents;
        this.msg = plugin.prefix + ChatColor.GREEN + "Update available! Type " + ChatColor.YELLOW + "/reload confirm";
    }

    /**
     * Runs an update as long as config allows, player is Op and
     * there is not already an update queued.
     */
    @Override
    public void run() {

        // If not allowed by config or player is not Op, return
        if (!plugin.getConfig().getBoolean("AutoUpdate.enabled") || !player.isOp()) return;

        // Is there an update already queued, message and return
        if (ipEvents.isUpdateQueued()) { player.sendMessage(msg); return; }

        // Else check for update
        Updater updater = new Updater(plugin, 340506, plugin.getPluginFile(), Updater.UpdateType.DEFAULT, true);

        // Get, message and update result
        if (updater.getResult() == Updater.UpdateResult.SUCCESS) {
            player.sendMessage(msg);
            ipEvents.setUpdateQueued();
        }

    }

}

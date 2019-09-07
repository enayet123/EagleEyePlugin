import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

/**
 * Metadata used to differentiate potential protection
 * attempts in comparison to general wall sign usage.
 * Metadata is never written to disk and is therefore
 * volatile. Players must complete protection steps
 * without the server restarting and losing memory.
 */
public class ProtectableMeta implements MetadataValue {
    private Player player;
    private Block protectableBlock;

    /**
     * Constructor takes and stores block sign is placed against
     * @param player Player placing the sign
     * @param protectableBlock Potentially protectable block
     */
    public ProtectableMeta(Player player, Block protectableBlock) {
        this.player = player;
        this.protectableBlock = protectableBlock;
    }

    /**
     * Returns the block that can be protected
     * @return Protectable block
     */
    @Override
    public Object value() { return protectableBlock; }

    /**
     * Not in use
     */
    @Override
    public int asInt() { return 0; }

    /**
     * Not in use
     */
    @Override
    public float asFloat() { return 0; }

    /**
     * Not in use
     */
    @Override
    public double asDouble() { return 0; }

    /**
     * Not in use
     */
    @Override
    public long asLong() { return 0; }

    /**
     * Not in use
     */
    @Override
    public short asShort() { return 0; }

    /**
     * Not in use
     */
    @Override
    public byte asByte() { return 0; }

    /**
     * Not in use
     */
    @Override
    public boolean asBoolean() { return false; }

    /**
     * Not in use
     */
    @Override
    public String asString() { return player.getName(); }

    /**
     * Gets plugin that owns metadata
     * @return Plugin that sets/gets the metadata
     */
    @Override
    public Plugin getOwningPlugin() { return Bukkit.getPluginManager().getPlugin("EagleEye"); }

    /**
     * Not in use
     */
    @Override
    public void invalidate() {}

    /**
     * Provides debug information of player that
     * caused metadata to be created and the related
     * block
     * @return Human readable metadata
     */
    @Override
    public String toString() {
        ChatColor b = ChatColor.BLUE;
        ChatColor y = ChatColor.YELLOW;
        return String.format(
                "%s: %s\n%s: %s\n%s: X(%s) Y(%s) Z(%s)",
                b + "UUID", y + player.getUniqueId().toString(),
                b + "Username", y + player.getName(),
                b + "Protectable Block Location" + y,
                protectableBlock.getX(), protectableBlock.getY(), protectableBlock.getZ()
        );
    }

}

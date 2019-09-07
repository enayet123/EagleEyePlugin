package types;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility used to make dealing with wall signs easier
 */
public class Signs {

    private static final List<Material> signs = Arrays.asList(
            Material.ACACIA_WALL_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.DARK_OAK_WALL_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.OAK_WALL_SIGN,
            Material.SPRUCE_WALL_SIGN
    );

    /**
     * Checks if Material is a valid wall sign
     * @param m Material to check
     * @return Is Material a wall sign
     */
    public static boolean contains(Material m) { return signs.contains(m); }

    /**
     * Validates syntax of wall sign
     * @param e Event triggered when player is done writing
     *          text on a wall sign
     * @return Is syntax valid
     */
    public static boolean hasValidSyntax(SignChangeEvent e) {
        // Compress content
        String content = getCompressedText(e.getLines());
        // Defined regex (Starts and ends with ~ containing a-z, A-Z or 0-9 in between)
        // Between 3 to 16 characters for usernames as defined by Minecraft
        String regex = "@{1}[a-zA-Z0-9_-]{3,16}";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }

    /**
     * Removes all unnecessary characters from wall sign text
     * @param lines Lines from wall sign
     * @return Compressed/Trimmed wall sign text
     */
    private static String getCompressedText(String[] lines) {
        return String.join("", lines).replaceAll("[\\s\\n\\r\\t]+","");
    }

    /**
     * Gets username (from a syntax checked wall sign only)
     * @param lines Lines from wall sign
     * @return Compress/Trimmed and '@' removed wall sign text
     */
    public static String getUsername(String[] lines) {
        return getCompressedText(lines).substring(1);
    }

    /**
     * Checks if sign has metadata
     * @param sign Sign to check for metadata
     * @return Is metadata present
     */
    public static boolean hasMeta(Sign sign) {
        return sign.getMetadata("protect").size() > 0;
    }

    /**
     * Gets block stored in metadata
     * @param sign Sign containing metadata
     * @return Protectable Block the sign was placed against
     */
    public static Block getBlockFromMeta(Sign sign) {
        return (Block) sign.getMetadata("protect").get(0).value();
    }

}

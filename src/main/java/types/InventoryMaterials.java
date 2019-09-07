package types;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

/**
 * Allows for checking if blocks are protectable inventories
 */
public class InventoryMaterials {

    private final static List<org.bukkit.Material> materials = Arrays.asList(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.FURNACE,
            Material.HOPPER,
            //Material.STONECUTTER, // Not yet supported
            Material.DISPENSER,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.BARREL,
            Material.LOOM,
            //Material.CARTOGRAPHY_TABLE, // Not yet supported
            Material.DROPPER,
            Material.GRINDSTONE
    );

    /**
     * Checks if material is in list of protectable materials
     * @param m Material type
     * @return Can Material be protected
     */
    public static boolean contains(Material m) {
        return materials.contains(m);
    }

}

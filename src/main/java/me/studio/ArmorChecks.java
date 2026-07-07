
package me.studio;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ArmorChecks {

    private ArmorChecks() {}

    public static boolean hasFullArmorEquipped(Player p) {
        ItemStack helmet = p.getInventory().getHelmet();
        ItemStack chest = p.getInventory().getChestplate();
        ItemStack legs = p.getInventory().getLeggings();
        ItemStack boots = p.getInventory().getBoots();

        return isValid(helmet) && isValid(chest) && isValid(legs) && isValid(boots);
    }

    private static boolean isValid(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }
}

package kendo.me.kproffesionscore.builder.item;

import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    // Define o nome do item
    public ItemBuilder setName(String name) {
        if (meta != null) {
            meta.setDisplayName(ChatUtils.color(name));
            item.setItemMeta(meta);
        }
        return this;
    }

    // Define a lore
    public ItemBuilder setLore(List<String> lore) {
        if (meta != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatUtils.color(line));
            }
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }
        return this;
    }

    // Define CustomModelData
    public ItemBuilder setModelData(int modelData) {
        if (meta != null) {
            meta.setCustomModelData(modelData);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder setMaterial(Material material) {
        this.item.setType(material);
        return this;
    }

    public ItemStack build() {
        return item;
    }
}

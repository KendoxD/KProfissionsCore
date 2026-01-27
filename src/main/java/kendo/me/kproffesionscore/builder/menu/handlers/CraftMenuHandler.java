package kendo.me.kproffesionscore.builder.menu.handlers;

import kendo.me.kproffesionscore.builder.menu.Menu;
import kendo.me.kproffesionscore.builder.menu.enums.MenuType;
import kendo.me.kproffesionscore.crafts.CraftLoader;
import kendo.me.kproffesionscore.crafts.CraftRecipe;
import kendo.me.kproffesionscore.manager.config.ConfigUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class CraftMenuHandler extends MenuHandler implements Listener {
    private final Map<String, CraftRecipe> recipes;

    public CraftMenuHandler(JavaPlugin plugin, ConfigUtils config, CraftLoader loader) {
        super(plugin, config);
        this.recipes = loader.getRecipes();
    }

    public boolean isCraftingMenu( Menu menu){
        return menu.getType() != MenuType.MENU_CHOOSE;
    }


    @EventHandler
    public void onCraftClick(InventoryClickEvent e){
        Menu menu = getMenuByInventory(e);
        if(menu == null || isCraftingMenu(menu)) return;
        int rawSlot = e.getRawSlot();
        if (rawSlot >= menu.getInventory().getSize()) return;
        e.setCancelled(true); // Cancela apenas cliques no menu

        ItemStack cursor = e.getCursor();
        ItemStack clicked = e.getCurrentItem();



        menu.handleClose();
    }



    @EventHandler
    public void onCraftMenuClose(InventoryCloseEvent e) {
        Menu menu = getMenuByInventory(e);
        if (menu == null || !isCraftingMenu(menu)) return;

        // Remove os itens usados e devolve o que sobrou
        menu.getInventory().forEach(item -> {
            if (item != null) {
                menu.getPlayer().getInventory().addItem(item);
            }
        });
        menu.handleClose();
    }


}

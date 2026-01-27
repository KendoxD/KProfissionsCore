package kendo.me.kproffesionscore.builder.menu.handlers;

import kendo.me.kproffesionscore.builder.menu.Menu;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.professions.database.connection.ProfissionDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MenuHandler implements Listener {

    private final ConfigManager config;
    private final List<Menu> openMenus = new ArrayList<>();
    public MenuHandler(JavaPlugin plugin, ConfigManager config){
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.config = config;
    }
    /**
     * Adiciona a lista de menus abertos o objeto do menu
     * @param menu Objeto do menu a ser inserido
     */
    private void insertMenu(@NotNull Menu menu) {
        if(openMenus.contains(menu)) return;
        openMenus.add(menu);
    }


    /**
     * Responsavel por abrir o inventario e adicionar a lista de menus
     * @param menu
     */
    public void openMenu(@NotNull Menu menu){
        insertMenu(menu);
        Player player = menu.getPlayer();
        player.openInventory(menu.getInventory());
        player.updateInventory();
    }


    public void closeMenu(@NotNull Menu menu){
        menu.getPlayer().closeInventory();
        menu.getPlayer().updateInventory();
        removeMenuFromHandler(menu);
    }

    /**
     * Da clear na lista de menus abertos e fecha o inventario do player.
     */
    public void clear(){
        for(Menu menu : openMenus) {
            menu.getPlayer().closeInventory();
            menu.getPlayer().updateInventory();
            //fecha o inventario pra evitar bugs?
        }
        openMenus.clear();
    }

    private void removeMenuFromHandler(@NotNull Menu menu){
        openMenus.remove(menu);
    }
    private Menu getMenuByInventory(InventoryClickEvent event){
        return openMenus.stream()
                .filter(menu -> menu.getInventory().equals(event.getInventory()))
                .findFirst()
                .orElse(null);
    }
    private Menu getMenuByInventory(InventoryCloseEvent event){
        return openMenus.stream()
                .filter(menu -> menu.getInventory().equals(event.getInventory()))
                .findFirst()
                .orElse(null);
    }



    @EventHandler
    public void onClick(InventoryClickEvent e){
        Menu menu = getMenuByInventory(e);
        if(menu != null){
            e.setCancelled(true);
            int slot = e.getRawSlot();
            menu.handleMenuClick(slot);
        }

    }
    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Menu menu = getMenuByInventory(e);
        if(menu != null){
            menu.handleClose();
        }

    }
}

package kendo.me.kproffesionscore.builder.menu.handlers;

import kendo.me.kproffesionscore.builder.menu.Menu;
import kendo.me.kproffesionscore.builder.menu.enums.MenuType;
import kendo.me.kproffesionscore.crafts.ProfessionCraftItemLimit;
import kendo.me.kproffesionscore.manager.config.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MenuHandler implements Listener {

    private final ConfigUtils config;
    private final List<Menu> openMenus = new ArrayList<>();
    public MenuHandler(JavaPlugin plugin, ConfigUtils config){
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
    protected Menu getMenuByInventory(InventoryClickEvent event){
        return openMenus.stream()
                .filter(menu -> menu.getInventory().equals(event.getInventory()))
                .findFirst()
                .orElse(null);
    }
    protected Menu getMenuByInventory(InventoryCloseEvent event){
        return openMenus.stream()
                .filter(menu -> menu.getInventory().equals(event.getInventory()))
                .findFirst()
                .orElse(null);
    }


    private @NotNull Menu handleCraft(InventoryClickEvent e){
        return null;
    }



    @EventHandler
    public void onClick(InventoryClickEvent e){
        Menu menu = getMenuByInventory(e);
        int rawSlot = e.getRawSlot();
        if(menu == null) return;
        if (rawSlot < menu.getInventory().getSize()) {
            e.setCancelled(true); // Cancela apenas cliques dentro do menu
            menu.handleMenuClick(rawSlot);
        }

    }
    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Menu menu = getMenuByInventory(e);
        if(menu != null){
            for (ItemStack itemStack : menu.getInventory()) {
                if(itemStack != null && itemStack.getType() != Material.PAPER){
                    menu.getPlayer().getInventory().addItem(itemStack);
                }
            }
            menu.handleClose();
        }
    }

    @EventHandler
    public void onMedicMenuClick(InventoryClickEvent e) {
        Menu menu = getMenuByInventory(e);
        if(menu == null) return;
        if(menu.getType().equals(MenuType.MENU_MEDICO));
        Player player = menu.getPlayer();
        ItemStack item = e.getWhoClicked().getItemOnCursor();
        int [] validSlots = ProfessionCraftItemLimit.MEDICO.getSlots();
        for (int validSlot : validSlots) {
            if(e.getRawSlot() == validSlot){
                addItemToSlot(e,menu.getInventory(), validSlot, item);
            }
        }
    }


    /**
     * Metodo para adicionar item no slot dependendo do ClickType
     * @param e - evento de click no inventario
     * @param inv - inventario atual
     * @param validSlot - slot valido pra adicionar item
     * @param item - item no cursor do player;
     */
    private void addItemToSlot(InventoryClickEvent e , Inventory inv, int validSlot,ItemStack item){
        ItemStack current =  e.getInventory().getItem(e.getRawSlot());
        ItemStack itemToAdd = item.clone();
        if(e.getClick().isRightClick()){
            if(current == null|| current.getType() == Material.AIR) {
                itemToAdd.setAmount(1);
                inv.setItem(validSlot, itemToAdd);
            } else if(current.getType() == item.getType()){
                current.setAmount(current.getAmount()+1);
            }
            item.setAmount(item.getAmount()-1);
            if(item.getAmount() <= 0){
                item.setAmount(0);
            }
        } else if(e.getClick().isLeftClick()){
            if(current == null|| current.getType() == Material.AIR ) {
                if(e.getCursor() == null) return;
                inv.setItem(e.getRawSlot(), item.clone());
                item.setAmount(0);
            }
        }
    }

}
/**
 * [11:24:36] [Server thread/INFO]: Slots a trabalhar medico: 24
 * [11:24:38] [Server thread/INFO]: Slots a trabalhar medico: 21
 * [11:24:39] [Server thread/INFO]: Slots a trabalhar medico: 29
 * [11:24:40] [Server thread/INFO]: Slots a trabalhar medico: 19
 * [11:24:43] [Server thread/INFO]: Slots a trabalhar medico: 11
 */
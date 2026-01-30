package kendo.me.kproffesionscore.builder.menu.handlers;

import com.comphenix.protocol.ProtocolLib;
import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.menu.Menu;
import kendo.me.kproffesionscore.builder.menu.enums.MenuType;
import kendo.me.kproffesionscore.crafts.CraftLoader;
import kendo.me.kproffesionscore.crafts.CraftManager;
import kendo.me.kproffesionscore.crafts.ProfessionCraftItemLimit;
import kendo.me.kproffesionscore.manager.config.ConfigUtils;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuHandler implements Listener {

    private final ConfigUtils config;
    private final List<Menu> openMenus = new ArrayList<>();
    private final Map<Integer, ItemStack> mapCraft = new HashMap<>();

    public MenuHandler(JavaPlugin plugin, ConfigUtils config){
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.config = config;
    }

    private void insertMenu(@NotNull Menu menu) {
        if(openMenus.contains(menu)) return;
        openMenus.add(menu);
    }

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

    public void clear(){
        for(Menu menu : openMenus) {
            menu.getPlayer().closeInventory();
            menu.getPlayer().updateInventory();
        }
        openMenus.clear();
        mapCraft.clear();
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

    @EventHandler
    public void onClick(InventoryClickEvent e){
        Menu menu = getMenuByInventory(e);
        int rawSlot = e.getRawSlot();
        if(menu == null) return;
        if (rawSlot < menu.getInventory().getSize()) {
            e.setCancelled(true);

            // Slot 24 definido como o botão para finalizar o craft
            if (rawSlot == 24 && menu.getType().equals(MenuType.MENU_MEDICO)) {
                handleCraftFinalize((Player) e.getWhoClicked(), menu, "medico");
                return;
            }

            if(menu.getType().equals(MenuType.MENU_MEDICO)){
                onMedicMenu(e, menu);
            } else {
                menu.handleMenuClick(rawSlot);
            }
        }
    }


    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Menu menu = getMenuByInventory(e);
        if(menu != null){
            for (int slot : ProfessionCraftItemLimit.MEDICO.getSlots()) {
                ItemStack item = menu.getInventory().getItem(slot);
                if(item != null && item.getType() != Material.AIR && item.getType() != Material.PAPER){
                    menu.getPlayer().getInventory().addItem(item);
                }
            }
            mapCraft.clear();
            menu.handleClose();
            removeMenuFromHandler(menu);
        }
    }

    private void onMedicMenu(InventoryClickEvent e, Menu menu){
        ItemStack item = e.getWhoClicked().getItemOnCursor();
        int slot = e.getRawSlot();
        Player player = menu.getPlayer();
        int[] validSlots = ProfessionCraftItemLimit.MEDICO.getSlots();
        for (int validSlot : validSlots) {
            if(slot == validSlot){
                if (item.getType() == Material.AIR) {
                    Bukkit.getScheduler().runTask(KProfessionsCore.getInstance(), () -> {
                        ItemStack currentInSlot = menu.getInventory().getItem(slot);
                        if (currentInSlot == null || currentInSlot.getType() == Material.AIR) {
                            mapCraft.remove(slot);
                        } else {
                            mapCraft.put(slot, currentInSlot);
                        }
                        updatePreview(menu, "medico");
                    });
                    e.setCancelled(false);
                    return;
                }
                addItemToSlot(e, player, menu.getInventory(), validSlot, item);
                updatePreview(menu, "medico");
            }
        }
    }

    /**
     * Adiciona item no slot dependendo do ClickType (Right / Left click)
     * @param e
     * @param inv
     * @param validSlot
     * @param item
     */
    private void addItemToSlot(InventoryClickEvent e, Player player, Inventory inv, int validSlot, ItemStack item){
        ItemStack current = e.getInventory().getItem(e.getRawSlot());
        ItemStack itemToAdd = item.clone();

        if(e.getClick().isRightClick()){
            if(current == null || current.getType() == Material.AIR) {
                itemToAdd.setAmount(1);
                inv.setItem(validSlot, itemToAdd);
            } else if(current.getType() == item.getType()){
                current.setAmount(current.getAmount() + 1);
            }
            item.setAmount(item.getAmount() - 1);
            if(item.getAmount() <= 0){
                item.setAmount(0);
            }
        } else if(e.getClick().isLeftClick()){
            if(current == null || current.getType() == Material.AIR ) {
                if(e.getCursor() == null) return;
                inv.setItem(e.getRawSlot(), item.clone());
                item.setAmount(0);
            }
        }

        ItemStack finalItem = inv.getItem(validSlot);
        player.playSound(player.getLocation(), "my_sounds:click.button", 1f,1f);
        if (finalItem != null && finalItem.getType() != Material.AIR) {
            mapCraft.put(validSlot, finalItem);
        } else {
            mapCraft.remove(validSlot);
        }
    }

    /**
     * Atualiza o slot de item craftado dependendo do match
     * @param menu
     * @param profession
     */
    private void updatePreview(Menu menu, String profession) {
        Inventory inv = menu.getInventory();
        CraftManager craftManager = KProfessionsCore.getCraftManager();
        CraftLoader match = craftManager.findMatch(profession, mapCraft);

        if (match != null) {
            inv.setItem(24, match.getResult());
        } else {
            inv.setItem(24, new ItemStack(Material.AIR));
        }
    }

    /**
     * Chama a funcao de match do meu craftManager enviando o map atual dos slots de craft e manipula com base nos valores salvos.
     * @param player
     * @param menu
     * @param profession
     */
    private void handleCraftFinalize(Player player, Menu menu, String profession) {
        CraftManager craftManager = KProfessionsCore.getCraftManager();
        CraftLoader match = craftManager.findMatch(profession, mapCraft);

        if (match == null) return;

        Inventory inv = menu.getInventory();
        match.getIngredients().forEach((slot, ingredient) -> {
            ItemStack itemInSlot = inv.getItem(slot);
            if (itemInSlot != null) {
                int amountRemaining = itemInSlot.getAmount() - ingredient.getAmount();
                if (amountRemaining <= 0) {
                    inv.setItem(slot, null);
                    mapCraft.remove(slot);
                } else {
                    itemInSlot.setAmount(amountRemaining);
                    mapCraft.put(slot, itemInSlot);
                }
            }
        });
        player.getInventory().addItem(match.getResult());
        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1f, 1f); //
        updatePreview(menu, profession);
    }
}
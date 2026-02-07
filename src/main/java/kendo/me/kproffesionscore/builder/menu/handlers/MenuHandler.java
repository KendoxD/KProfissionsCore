package kendo.me.kproffesionscore.builder.menu.handlers;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.menu.Menu;
import kendo.me.kproffesionscore.builder.menu.enums.MenuType;
import kendo.me.kproffesionscore.crafts.CraftLoader;
import kendo.me.kproffesionscore.crafts.CraftManager;
import kendo.me.kproffesionscore.professions.database.connection.dao.MedicoDao;
import kendo.me.kproffesionscore.utils.ChatUtils;
import kendo.me.kproffesionscore.utils.ConfigUtils;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
        if(menu == null) return;

        int rawSlot = e.getRawSlot();
        if (rawSlot >= menu.getInventory().getSize()) return;

        String profession = menu.getType().name().toLowerCase().replace("menu_", "");

        if (rawSlot < menu.getInventory().getSize()) {
            e.setCancelled(true);

            if (rawSlot == 24 && !menu.getType().equals(MenuType.MENU_CHOOSE)) {
                handleCraftFinalize((Player) e.getWhoClicked(), menu, profession);
                return;
            }

            if(menu.getType().equals(MenuType.MENU_CHOOSE)){
                menu.handleMenuClick(rawSlot);
            } else {
                int[] validSlots = menu.getType().getSlots();
                for (int validSlot : validSlots) {
                    if(rawSlot == validSlot){
                        onProfessionMenu(e, menu, profession);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Menu menu = getMenuByInventory(e);
        if(menu != null){
            if(!menu.getType().equals(MenuType.MENU_CHOOSE)) {
                int[] craftSlots = menu.getType().getSlots();

                for (int slot : craftSlots) {
                    if (slot == 24) continue;
                    ItemStack item = menu.getInventory().getItem(slot);
                    if(item != null && item.getType() != Material.AIR){
                        if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
                            if (item.getItemMeta().getCustomModelData() == 30000) continue;
                        }
                        if (item.getType() == Material.BARRIER) continue;
                        menu.getPlayer().getInventory().addItem(item);
                    }
                }
            }

            mapCraft.clear();
            menu.handleClose();
            removeMenuFromHandler(menu);
        }
    }

    private void onProfessionMenu(InventoryClickEvent e, Menu menu, String profession){
        ItemStack item = e.getWhoClicked().getItemOnCursor();
        int slot = e.getRawSlot();
        Player player = menu.getPlayer();

        if (item.getType() == Material.AIR) {
            Bukkit.getScheduler().runTask(KProfessionsCore.getInstance(), () -> {
                ItemStack currentInSlot = menu.getInventory().getItem(slot);
                if (currentInSlot == null || currentInSlot.getType() == Material.AIR) {
                    mapCraft.remove(slot);
                } else {
                    mapCraft.put(slot, currentInSlot);
                }
                updatePreview(menu, profession);
            });
            e.setCancelled(false);
            return;
        }
        addItemToSlot(e, player, menu.getInventory(), slot, item);
        updatePreview(menu, profession);
    }

    /**
     * Adiciona item no slot dependendo do ClickType (Right / Left click)
     * @param e
     * @param inv
     * @param validSlot
     * @param item
     */
    private void addItemToSlot(InventoryClickEvent e, Player player, Inventory inv, int validSlot, ItemStack item){
        ItemStack current = inv.getItem(validSlot);
        ItemStack itemToAdd = item.clone();

        if(e.getClick().isRightClick()){
            if(current == null || current.getType() == Material.AIR) {
                itemToAdd.setAmount(1);
                inv.setItem(validSlot, itemToAdd);
            } else if(current.isSimilar(item)){
                current.setAmount(current.getAmount() + 1);
            }
            item.setAmount(item.getAmount() - 1);
        } else if(e.getClick().isLeftClick()){
            inv.setItem(validSlot, item.clone());
            item.setAmount(0);
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
     * Atualiza o slot de item craftado dependendo do match e do NÍVEL.
     * @param menu
     * @param profession
     */
    private void updatePreview(Menu menu, String profession) {
        Inventory inv = menu.getInventory();
        CraftManager craftManager = KProfessionsCore.getCraftManager();
        CraftLoader match = craftManager.findMatch(profession, mapCraft);

        if (match != null) {
            Player player = menu.getPlayer();
            var db = KProfessionsCore.getDatabase();
            int playerLevel = (db != null) ? db.getPlayerLevel(player.getDisplayName(), profession) : 0;

            if (playerLevel < match.getLevelRequired()) {
                ItemStack errorItem = new ItemStack(Material.BARRIER);
                ItemMeta meta = errorItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatUtils.color("&c&lBloqueado!"));
                    meta.setLore(Arrays.asList(
                            ChatUtils.color("&7Nível necessário: &e" + match.getLevelRequired()),
                            ChatUtils.color("&7Seu nível: &c" + playerLevel)
                    ));
                    errorItem.setItemMeta(meta);
                }
                inv.setItem(24, errorItem);
            } else {
                inv.setItem(24, match.getResult());
            }
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

        int playerLevel = KProfessionsCore.getDatabase().getPlayerLevel(player.getDisplayName(), profession);

        if (playerLevel < match.getLevelRequired()) {
            player.sendMessage(ChatUtils.color("&cVocê não possui nível de " + profession + " suficiente!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

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
        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1f, 1f);

        applyCraftExperience(player, match, profession);

        updatePreview(menu, profession);
    }

    /**
     * Calcula e salva o XP do jogador baseado na dificuldade do craft.
     */
    private void applyCraftExperience(Player player, CraftLoader match, String profession) {
        var db = KProfessionsCore.getDatabase();
        int playerLevel = db.getPlayerLevel(player.getDisplayName(), profession);
        int craftLevel = match.getLevelRequired();
        double baseExp = match.getExpGain();
        double expGain = config.calculateDynamicExp(baseExp, playerLevel, craftLevel);

        if (profession.equalsIgnoreCase("medico")) {
            var dao = new MedicoDao(db.getConnection());
            var medico = dao.load(player.getDisplayName());
            if (medico != null) {
                medico.addExp(expGain);
                dao.save(medico);
                player.sendMessage(ChatUtils.color("&a+" + String.format("%.1f", expGain) + " XP de Médico!"));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2f);
            }
        }
    }
}
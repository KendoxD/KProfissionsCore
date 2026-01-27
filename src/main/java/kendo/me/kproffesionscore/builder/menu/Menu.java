package kendo.me.kproffesionscore.builder.menu;

import kendo.me.kproffesionscore.builder.item.ItemBuilder;
import kendo.me.kproffesionscore.builder.menu.enums.MenuType;
import kendo.me.kproffesionscore.manager.config.ConfigUtils;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Menu {
    private static ConfigUtils manager = new ConfigUtils();
    private Player player;
    private BiConsumer<Player, Integer> onClick;
    private Consumer<Player> onClose;
    private Inventory inventory;
    private MenuType type;

    public Menu(@NotNull Player player, @NotNull MenuType type){
        this.player = player;
        this.type = type;
        this.inventory = Bukkit.createInventory(player, type.getSize(), type.getTitle(manager));
    }


    public Inventory getInventory() {
        return inventory;
    }

    public static ConfigUtils getConfig() {
        return manager;
    }

    public Player getPlayer() {
        return player;
    }

    public MenuType getType() {
        return type;
    }

    public void setType(MenuType type) {
        this.type = type;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public static void setManager(ConfigUtils manager) {
        Menu.manager = manager;
    }

    public void setInv(Inventory inventory) {
        this.inventory = inventory;
    }

    public void fullFillInventory(){
        //30000 - item blank
        ItemBuilder item = new ItemBuilder(Material.PAPER)
                .setName(ChatUtils.color("&c-"))
                .setModelData(30000);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, item.build());
        }
    }

    public void setOnClick(BiConsumer<Player, Integer> onClick){
        this.onClick = onClick;
    }

    public void setOnClose(Consumer<Player> onClose){
        this.onClose = onClose;
    }


    public void handleMenuClick(int slot){
       if(onClick != null){
           onClick.accept(player,slot);
       }
    }

    public void handleClose(){
        if(onClose != null){
            onClose.accept(player);
        }
    }
}
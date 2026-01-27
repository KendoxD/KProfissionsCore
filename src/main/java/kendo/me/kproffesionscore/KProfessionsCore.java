package kendo.me.kproffesionscore;

import kendo.me.kproffesionscore.builder.menu.handlers.MenuHandler;
import kendo.me.kproffesionscore.commands.profession.ProfessionCommand;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.professions.database.connection.ProfissionDatabase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class KProfessionsCore extends JavaPlugin {

    private MenuHandler menuHandler;
    private static ProfissionDatabase dbManager;


    @Override
    public void onEnable() {
       Bukkit.getLogger().info("[KProfessionsCore] Initialized!");
        saveDefaultConfig();
        menuHandler = new MenuHandler(getInstance(),new ConfigManager());
        registerCommands();
         dbManager = new ProfissionDatabase(getDataFolder());
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("[KProfessionsCore] Disabled!");
        menuHandler.clear();
    }

    public static JavaPlugin getInstance(){
        return getPlugin(KProfessionsCore.class);
    }

    public static ProfissionDatabase getDatabase(){
        return dbManager;
    };

    private void registerCommands(){
        new ProfessionCommand(this, menuHandler);
    }
}

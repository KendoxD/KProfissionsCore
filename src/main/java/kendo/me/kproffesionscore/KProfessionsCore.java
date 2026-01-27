package kendo.me.kproffesionscore;

import kendo.me.kproffesionscore.builder.menu.handlers.MenuHandler;
import kendo.me.kproffesionscore.commands.profession.ProfessionCommand;
import kendo.me.kproffesionscore.commands.profession.admin.ReloadCommand;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.manager.config.ConfigUtils;
import kendo.me.kproffesionscore.professions.database.connection.ProfissionDatabase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class KProfessionsCore extends JavaPlugin {

    private MenuHandler menuHandler;
    private static ProfissionDatabase dbManager;
    private static ConfigManager config;

    @Override
    public void onEnable() {
        Bukkit.getLogger().severe("Initialized!");

        saveDefaultConfig();

        config = new ConfigManager(this);
        try {
            config.initDirectorys();
            config.initFixedFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        menuHandler = new MenuHandler(getInstance(), new ConfigUtils());
        registerCommands();

        dbManager = new ProfissionDatabase(getDataFolder());
    }


    @Override
    public void onDisable() {
        Bukkit.getLogger().severe("Disabled!");
        menuHandler.clear();
    }

    public static JavaPlugin getInstance(){
        return getPlugin(KProfessionsCore.class);
    }

    public static ProfissionDatabase getDatabase(){
        return dbManager;
    };

    public static ConfigManager getConfigManager() {
        return config;
    }

    private void registerCommands(){
        new ProfessionCommand(this, menuHandler);
        new ReloadCommand(this, config);
    }
}

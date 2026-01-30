package kendo.me.kproffesionscore;

import kendo.me.kproffesionscore.builder.menu.handlers.MenuHandler;
import kendo.me.kproffesionscore.commands.profession.ProfessionCommand;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.ReloadCommand;
import kendo.me.kproffesionscore.crafts.CraftManager;
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
    private static CraftManager craftManager;

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
        craftManager = new CraftManager(this, config);
        menuHandler = new MenuHandler(getInstance(), new ConfigUtils());
        registerCommands();

        dbManager = new ProfissionDatabase(getDataFolder());
        craftManager.loadAll();
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
    public static CraftManager getCraftManager() {
        return craftManager;
    }


    private void registerCommands(){
        new ProfessionCommand(this, menuHandler);
        new ReloadCommand(this, config);
    }


}

///
// Core:
// todo: adicionar o resto dos Dao - pensar na questao de level up, pensar na config de profissao (sem craft)
// Crafts
// TODO:
//  - comando pra remover craft
//  - arrumar menu handler pra lidar com a questao dos crafts
//  - Conquistas crafts
//  - adicionar raridade de crafts - adicioar cap de craft q ser feito dependendo do lvl
//
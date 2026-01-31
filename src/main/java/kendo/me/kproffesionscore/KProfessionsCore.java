package kendo.me.kproffesionscore;

import kendo.me.kproffesionscore.entities.SeringaProjectil;
import kendo.me.kproffesionscore.entities.events.SeringeEvent;
import kendo.me.kproffesionscore.entities.manager.EntityManager;
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
    private static EntityManager entityManager;
    @Override
    public void onEnable() {
        Bukkit.getLogger().severe("Initialized!");

        entityManager = new EntityManager();
        // Roda a cada 1 tick (0.05 segundos)
        entityManager.runTaskTimer(this, 0L, 1L);
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
        registerEvents();
        dbManager = new ProfissionDatabase(getDataFolder());
        craftManager.loadAll();
    }


    @Override
    public void onDisable() {
        Bukkit.getLogger().severe("Disabled!");
        menuHandler.clear();
        entityManager.removeAllEntities();
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

    public static EntityManager getEntityManager(){
        return  entityManager;
    }

    private void registerCommands(){
        new ProfessionCommand(this, menuHandler);
        new ReloadCommand(this, config);
    }

    private void registerEvents(){
        new SeringeEvent(this);
    }



}

///
// Core:
// todo: adicionar o resto dos Dao - pensar na questao de level up, pensar na config de profissao (sem craft)
// Crafts
// todo: deixar seringas configuraveis na config de medico.yml (fora do path crafs)
// TODO:
//  - comando pra remover craft
//  - Conquistas crafts
//  - adicionar raridade de crafts - adicioar cap de craft q ser feito dependendo do lvl
//
package kendo.me.kproffesionscore;

import kendo.me.kproffesionscore.commands.profession.ProfessionPerfilCommand;
import kendo.me.kproffesionscore.entities.events.combatente.SwordAttackTest;
import kendo.me.kproffesionscore.entities.events.medic.*;
import kendo.me.kproffesionscore.entities.manager.EntityManager;
import kendo.me.kproffesionscore.builder.menu.handlers.MenuHandler;
import kendo.me.kproffesionscore.commands.profession.ProfessionCommand;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.ReloadCommand;
import kendo.me.kproffesionscore.crafts.CraftManager;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.utils.ChatUtils;
import kendo.me.kproffesionscore.utils.ConfigUtils;
import kendo.me.kproffesionscore.manager.config.CooldownsManager;
import kendo.me.kproffesionscore.professions.database.connection.ProfissionDatabase;
import kendo.me.kproffesionscore.utils.skript.SkriptUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public final class KProfessionsCore extends JavaPlugin {

    private MenuHandler menuHandler;
    private static ProfissionDatabase dbManager;
    private static ConfigManager config;
    private static CraftManager craftManager;
    private static EntityManager entityManager;
    private ConfigUtils configUtils = new ConfigUtils();
    private static CooldownsManager cooldownsManager = new CooldownsManager();

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

        dbManager = new ProfissionDatabase(getDataFolder());
        entityManager = new EntityManager();
        entityManager.runTaskTimer(this, 0L, 1L);
        craftManager = new CraftManager(this, config);
        menuHandler = new MenuHandler(getInstance(), configUtils);

        registerCommands();
        registerEvents();

        craftManager.loadAll();

        // Debug HP ActionBar
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    double currentHp = SkriptUtils.getSkriptVariable(player, "hp");
                    double maxHp = SkriptUtils.getSkriptVariable(player, "hpmax");
                    String subtitle = ChatUtils.color("&c&lHP: &f" + (int)currentHp + "&7/&f" + (int)maxHp);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.color(subtitle)));
                }
            }
        }.runTaskTimer(this, 0L, 5L);
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().severe("Disabled!");
        if (menuHandler != null) menuHandler.clear();
        if (entityManager != null) entityManager.removeAllEntities();
        cooldownsManager.cleanExpired();
    }

    public static JavaPlugin getInstance(){ return getPlugin(KProfessionsCore.class); }
    public static ProfissionDatabase getDatabase(){ return dbManager; }
    public static ConfigManager getConfigManager() { return config; }
    public static CraftManager getCraftManager() { return craftManager; }
    public static EntityManager getEntityManager(){ return entityManager; }
    public static CooldownsManager getCooldownsManager(){ return cooldownsManager; }

    private void registerCommands(){
        new ProfessionCommand(this, menuHandler);
        new ProfessionPerfilCommand(this);
        new ReloadCommand(this, config);
    }

    private void registerEvents(){
        new GreenSeringeEvent(this);
        new RedSeringeEvent(this);
        new MedicKitEvent(this);
        new ThrowableMedicKit(this);
        new SwordAttackTest(this);
        new BandageConsumeEvent(this);
    }
}
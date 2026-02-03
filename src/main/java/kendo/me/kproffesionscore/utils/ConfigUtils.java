package kendo.me.kproffesionscore.utils;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.manager.config.paths.ConfigFiles;
import kendo.me.kproffesionscore.manager.config.paths.ConfigPaths;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigUtils {
    public String getTitleChooseMenu(){
        return KProfessionsCore.getInstance().getConfig().getString("menu.choose");
    }

    public String getTitleMedicMenu(){
        return KProfessionsCore.getInstance().getConfig().getString("menu.medicGui");
    }
    public String getTitleCombatenteMenu(){
        return KProfessionsCore.getInstance().getConfig().getString("menu.combatenteGui");
    }
    public String getTitleCozinheironteMenu(){
        return KProfessionsCore.getInstance().getConfig().getString("menu.cozinheiroGui");
    }

    /**
     * Retorna o arquivo yml das skills dependendo da profissao
     * @param profission
     * @return config
     */
    public YamlConfiguration getConfigFile(String profission){
        YamlConfiguration config = null;
        ConfigManager configManager = KProfessionsCore.getConfigManager();
        for (ConfigFiles file : ConfigFiles.values()) {
            if (file.name().equalsIgnoreCase(profission)) {
                config = configManager.getCraftFile(ConfigPaths.PROFISSOES, file);
                break;
            }
        }
        return config;
    }
}

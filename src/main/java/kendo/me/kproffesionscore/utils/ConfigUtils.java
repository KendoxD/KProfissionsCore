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
    /**
     * Calcula o XP dinâmico com base na diferença de nível entre o jogador e a skill.
     * @param baseExp XP base configurado no arquivo YML.
     * @param playerLevel Nível atual do jogador na profissão.
     * @param requiredLevel Nível mínimo exigido para usar a skill/craft.
     * @return XP calculado com redução por nível alto.
     */
    public double calculateDynamicExp(double baseExp, int playerLevel, int requiredLevel) {
        int diff = playerLevel - requiredLevel;
        if (diff <= 0) return baseExp;
        double factor = 1.0 - (diff * 0.05);
        if (factor < 0.10) factor = 0.10;
        return baseExp * factor;
    }
}

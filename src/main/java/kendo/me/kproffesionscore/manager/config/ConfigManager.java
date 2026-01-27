package kendo.me.kproffesionscore.manager.config;

import kendo.me.kproffesionscore.KProfessionsCore;

public class ConfigManager {
    public String getTitleChooseMenu(){
        return KProfessionsCore.getInstance().getConfig().getString("menu.choose");
    }

    public String getTitleMedicMenu(){
        return KProfessionsCore.getInstance().getConfig().getString("menu.medicGui");
    }
}

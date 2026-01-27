package kendo.me.kproffesionscore.builder.menu.enums;

import kendo.me.kproffesionscore.manager.config.ConfigUtils;
import kendo.me.kproffesionscore.utils.ChatUtils;

public enum MenuType {
    MENU_FORJA{
        @Override
        public String getTitle(ConfigUtils manager){
            return "";
        }

        @Override
        public int getSize(){
            return size;
        }
    },
    MENU_MEDICO {
        @Override
        public String getTitle(ConfigUtils manager) {
            return ChatUtils.color(manager.getTitleMedicMenu());
        }
        @Override
        public int getSize(){
            return size;
        }
    },
    MENU_COZINHEIRO {
        @Override
        public String getTitle(ConfigUtils manager) {
            return "";
        }
        @Override
        public int getSize(){
            return size;
        }
    },
    MENU_CHOOSE {
        @Override
        public String getTitle(ConfigUtils manager) {
            return ChatUtils.color(manager.getTitleChooseMenu());
        }
        @Override
        public int getSize(){
            return size;
        }
    };

    public abstract String getTitle(ConfigUtils manager);
    public abstract int getSize();
    private static final int size = 45;
}

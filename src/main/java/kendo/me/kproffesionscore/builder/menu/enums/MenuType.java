package kendo.me.kproffesionscore.builder.menu.enums;

import kendo.me.kproffesionscore.utils.ConfigUtils;
import kendo.me.kproffesionscore.utils.ChatUtils;

public enum MenuType {
    MENU_FORJA(new int[]{}){
        @Override
        public String getTitle(ConfigUtils manager){
            return "";
        }

        @Override
        public int getSize(){
            return size;
        }
    },
    MENU_MEDICO(new int[]{29, 21, 19, 11,24}) {
        @Override
        public String getTitle(ConfigUtils manager) {
            return ChatUtils.color(manager.getTitleMedicMenu());
        }
        @Override
        public int getSize(){
            return size;
        }
    },
    MENU_COZINHEIRO(new int[]{}) {
        @Override
        public String getTitle(ConfigUtils manager) {
            return "";
        }
        @Override
        public int getSize(){
            return size;
        }
    },
    MENU_CHOOSE(new int[]{}) {
        @Override
        public String getTitle(ConfigUtils manager) {
            return ChatUtils.color(manager.getTitleChooseMenu());
        }
        @Override
        public int getSize(){
            return size;
        }
    };

    MenuType(int[] slots) {
        this.slots = slots;
    }

    public abstract String getTitle(ConfigUtils manager);
    public abstract int getSize();
    private static final int size = 45;

    public int[] getSlots() {
        return slots;
    }
    private final int[] slots;
}

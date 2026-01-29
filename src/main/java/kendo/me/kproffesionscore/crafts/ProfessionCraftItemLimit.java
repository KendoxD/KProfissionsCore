package kendo.me.kproffesionscore.crafts;

public enum ProfessionCraftItemLimit {

    MEDICO(4, new int[]{29, 21, 19, 11}),
    COMBATENTE(6, new int[]{20}),
    COZINHEIRO(5, new int[]{})
    ;

    private final int maxIngredients;
    private final int [] slots;
    ProfessionCraftItemLimit(int maxIngredients, int[] slots) {
        this.maxIngredients = maxIngredients;
        this.slots = slots;
    }

    public int getMaxIngredients() {
        return maxIngredients;
    }

    public int[] getSlots() {
        return slots;
    }

    public static int getLimit(String profession) {
        try {
            return ProfessionCraftItemLimit
                    .valueOf(profession.toUpperCase())
                    .getMaxIngredients();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
    public static int[] getValidSlots(String profession){
        return ProfessionCraftItemLimit
                .valueOf(profession.toUpperCase())
                .getSlots();
    }
}

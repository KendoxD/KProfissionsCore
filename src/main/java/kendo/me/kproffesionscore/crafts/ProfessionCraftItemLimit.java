package kendo.me.kproffesionscore.crafts;

public enum ProfessionCraftItemLimit {

    MEDICO(4),
    COMBATENTE(6),
    COZINHEIRO(5);

    private final int maxIngredients;

    ProfessionCraftItemLimit(int maxIngredients) {
        this.maxIngredients = maxIngredients;
    }

    public int getMaxIngredients() {
        return maxIngredients;
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
}

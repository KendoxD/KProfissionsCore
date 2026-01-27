package kendo.me.kproffesionscore.crafts;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CraftRecipe {
    private final Map<Integer, ItemStack> ingredients;
    private final ItemStack craftResult;

    public CraftRecipe(Map<Integer, ItemStack> ingredients, ItemStack craftResult){
        this.ingredients = ingredients;
        this.craftResult = craftResult;
    }

    public Map<Integer, ItemStack> getIngredients() {
        return ingredients;
    }

    public ItemStack getResult() {
        return craftResult;
    }
}

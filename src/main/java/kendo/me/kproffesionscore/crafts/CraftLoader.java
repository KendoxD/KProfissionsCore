package kendo.me.kproffesionscore.crafts;

import org.bukkit.inventory.ItemStack;
import java.util.Map;

public class CraftLoader {
    private final String recipeName;
    private final String profession;
    private final Map<Integer, ItemStack> ingredients;
    private final ItemStack craftResult;
    private final int levelRequired; // Novo campo

    public CraftLoader(String recipeName, String profession, Map<Integer, ItemStack> ingredients, ItemStack craftResult, int levelRequired){
        this.recipeName = recipeName;
        this.profession = profession;
        this.ingredients = ingredients;
        this.craftResult = craftResult;
        this.levelRequired = levelRequired;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public String getProfession() {
        return profession;
    }

    public ItemStack getCraftResult() {
        return craftResult;
    }

    public Map<Integer, ItemStack> getIngredients() {
        return ingredients;
    }

    public ItemStack getResult() {
        return craftResult;
    }

    public int getLevelRequired() {
        return levelRequired;
    }
}
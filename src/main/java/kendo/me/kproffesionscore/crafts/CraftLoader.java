package kendo.me.kproffesionscore.crafts;

import java.util.HashMap;
import java.util.Map;

public class CraftLoader {

    private final Map<String, CraftRecipe> recipes = new HashMap<>();
    public Map<String, CraftRecipe> getRecipes() {
        return recipes;
    }
}

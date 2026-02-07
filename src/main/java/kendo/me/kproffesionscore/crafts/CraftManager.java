package kendo.me.kproffesionscore.crafts;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.manager.config.paths.ConfigFiles;
import kendo.me.kproffesionscore.manager.config.paths.ConfigPaths;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    private final Map<String, List<CraftLoader>> recipesByProfession = new HashMap<>();

    public CraftManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Carrega todos os crafts de todas as profissoes existentes.
     */
    public void loadAll(){
        recipesByProfession.clear(); // limpa o que tiver em memoria

        for (ConfigFiles value : ConfigFiles.values()) {
            String profissionName = value.name().toLowerCase();
            YamlConfiguration config = configManager.getCraftFile(ConfigPaths.CRAFTS, value);
            if(config == null){
                System.out.println("Error loading config for profession: " + profissionName);
                continue;
            }
            if(!config.contains("Craft")) continue;

            List<CraftLoader> craftList = new ArrayList<>();
            for (String craftKey : config.getConfigurationSection("Craft").getKeys(false)) {
                String path = "Craft." + craftKey;

                ItemStack resultItem = config.getItemStack(path + ".result.item");
                int levelRequired = config.getInt(path + ".level-required");

                Map<Integer, ItemStack> ingredients = new HashMap<>();
                if(config.contains(path + ".ingredients")){
                    for (String slotKey : config.getConfigurationSection(path + ".ingredients").getKeys(false)) {
                        int slot = Integer.parseInt(slotKey);
                        ItemStack item = config.getItemStack(path + ".ingredients." + slotKey + ".item");
                        ingredients.put(slot, item);
                    }
                }
                craftList.add(new CraftLoader(craftKey, profissionName, ingredients, resultItem, levelRequired));
            }
            recipesByProfession.put(profissionName, craftList);
        }
        Bukkit.getLogger().severe("All crafts loaded successfully! Professions: " + recipesByProfession.size());
    }

    /**
     * Verifica se aquele padrao de craft existe dentro dos crafts na config
     */
    public CraftLoader findMatch(@NotNull String profession, Map<Integer, ItemStack> currentGrid) {
        List<CraftLoader> options = recipesByProfession.get(profession);
        if (options == null) return null;
        for (CraftLoader recipe : options) {
            if (recipe.getIngredients().size() != currentGrid.size()) continue;

            boolean match = true;
            for (Map.Entry<Integer, ItemStack> entry : recipe.getIngredients().entrySet()) {
                ItemStack placed = currentGrid.get(entry.getKey());
                if (placed == null || !placed.isSimilar(entry.getValue()) || placed.getAmount() < entry.getValue().getAmount()) {
                    match = false;
                    break;
                }
            }

            if (match) return recipe;
        }
        return null;
    }

    // Only Debug
    public List<String> getAllCraftNames() {
        List<String> names = new ArrayList<>();
        for (List<CraftLoader> recipes : recipesByProfession.values()) {
            for (CraftLoader recipe : recipes) {
                names.add(recipe.getRecipeName());
            }
        }
        return names;
    }

    // Only Debug
    public void debugLoadedCrafts() {
        if (recipesByProfession.isEmpty()) {
            System.out.println(ChatUtils.color("&cCache vazio! Use /profissoes reload."));
            return;
        }
        recipesByProfession.forEach((profession, recipes) -> {
            System.out.println(ChatUtils.color("&eProfissão: &6" + profession.toUpperCase()));
            for (CraftLoader craft : recipes) {
                int ingredSize = craft.getIngredients().size();
                String resultName = (craft.getResult() != null) ? craft.getResult().getType().name() : "ERRO-RESULTADO";

                System.out.println(ChatUtils.color(" &8- &f" + craft.getRecipeName() +
                        " &7[Nível " + craft.getLevelRequired() + "] &7(" + ingredSize + " ingredientes) -> &a" + resultName));

                craft.getIngredients().forEach((slot, item) -> {
                    String name = (item != null) ? item.getType().name() : "NULO";
                    System.out.println("  Slot " + slot + " : " + name + " (x" + (item != null ? item.getAmount() : 0) + ")");
                });
            }
        });
    }
}
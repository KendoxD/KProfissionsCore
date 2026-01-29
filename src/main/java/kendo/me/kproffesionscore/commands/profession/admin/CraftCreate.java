package kendo.me.kproffesionscore.commands.profession.admin;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.crafts.ProfessionCraftItemLimit;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.manager.config.paths.ConfigFiles;
import kendo.me.kproffesionscore.manager.config.paths.ConfigPaths;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class CraftCreate extends CommandBuilder {

    public CraftCreate(JavaPlugin plugin) {
        super(plugin, "profissoes");
    }

    public void execute(Player player, @Nullable String[] args) {

        if (args == null || args.length < 5) {
            player.sendMessage(ChatUtils.color("&cUse: /profissoes admin craft <create|add> <profissao> <nomeCraft> <slot(opcional no create)>"));
            return;
        }

        // args[1] = craft
        if (!args[1].equalsIgnoreCase("craft")) {
            return;
        }

        String action = args[2].toLowerCase();     // create | add
        String profissao = args[3].toLowerCase();
        String craftName = args[4];

        ConfigManager configManager = KProfessionsCore.getConfigManager();
        YamlConfiguration config = null;

        // Busca o arquivo correto da profissão
        for (ConfigFiles file : ConfigFiles.values()) {
            if (file.name().equalsIgnoreCase(profissao)) {
                config = configManager.getCraftFile(ConfigPaths.CRAFTS, file);
                break;
            }
        }

        if (config == null) {
            player.sendMessage(ChatUtils.color("&cProfissão inválida: " + profissao));
            return;
        }

        switch (action) {
            case "create" -> {
                if (player.getItemInHand().getType() == Material.AIR) {
                    player.sendMessage(ChatUtils.color("&cColoque o item RESULTADO do craft na mão!"));
                    return;
                }


                if (configManager.checkIfCraftExists(craftName)) {
                    player.sendMessage(ChatUtils.color("&cEsse craft já existe!"));
                    return;
                }
                config.set("Craft." + craftName + ".result.item", player.getItemInHand());
                config.createSection("Craft." + craftName + ".ingredients");

                configManager.saveYaml(config, ConfigPaths.CRAFTS.getPath(), profissao + ".yml");

                player.sendMessage(ChatUtils.color("&aCraft criado com sucesso!"));
            }

            //TODO: refactor code?
            case "add" -> {
                if (args.length < 6) {
                    player.sendMessage(ChatUtils.color("&cUse: /profissoes admin craft add <profissao> <nomeCraft> <slot>"));
                    return;
                }
                if (player.getItemInHand().getType() == Material.AIR) {
                    player.sendMessage(ChatUtils.color("&cColoque o item INGREDIENTE na mão!"));
                    return;
                }

                int slot;
                boolean exists = false;
                int [] craftSlots = ProfessionCraftItemLimit.getValidSlots(profissao);
                try {
                    slot = Integer.parseInt(args[5]);
                    for (int craftSlot : craftSlots) {
                        if (slot == craftSlot) {
                            exists = true;
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatUtils.color("&cSlot inválido!"));
                    return;
                }
                if(!exists || slot > 45 || slot <=0){
                    player.sendMessage(ChatUtils.color("&cSlot inválido!"));
                    player.sendMessage(ChatUtils.color("&aSlots validos: " + Arrays.toString(Arrays.stream(Arrays.stream(craftSlots).toArray()).sorted().toArray())));
                    return;
                }
                if (!configManager.checkIfCraftExists(craftName)) {
                    System.out.println(craftName);
                    player.sendMessage(ChatUtils.color("&cEsse craft não existe!"));
                    return;
                }
                int max = ProfessionCraftItemLimit.getLimit(profissao);
                int current = configManager.getIngredientSize(craftName);

                if (current >= max) {
                    player.sendMessage(ChatUtils.color("&cEsse craft já atingiu o limite de " + max + " ingredientes."));
                    return;
                }
                if (configManager.checkIfSlotIsOcuppied(craftName, slot)) {
                    player.sendMessage(ChatUtils.color("&cJá existe um item nesse slot!"));
                    return;
                }

                config.set("Craft." + craftName+".ingredients" + "." + slot + ".item", player.getItemInHand());
                configManager.saveYaml(config, ConfigPaths.CRAFTS.getPath(), profissao + ".yml");

                player.sendMessage(ChatUtils.color("&aIngrediente adicionado no slot &e" + slot));
            }

            default -> player.sendMessage(ChatUtils.color("&cAção inválida: " + action));
        }
    }
}

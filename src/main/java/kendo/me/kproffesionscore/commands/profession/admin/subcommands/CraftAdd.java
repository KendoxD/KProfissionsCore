package kendo.me.kproffesionscore.commands.profession.admin.subcommands;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.commands.action.CommandAction;
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

public class CraftAdd extends CommandBuilder implements CommandAction {

    public CraftAdd(JavaPlugin plugin) {
        super(plugin, "profissoes");
    }

    @Override
    public void execute(Player player, @Nullable String[] args) {
        // Validação de tamanho dos argumentos
        if (args == null || args.length < 6) {
            player.sendMessage(ChatUtils.color("&cUse: /profissoes admin craft add <profissao> <nomeCraft> <slot>"));
            return;
        }

        // Definindo as variáveis baseadas nos argumentos passados
        String profissao = args[3].toLowerCase();
        String craftName = args[4];
        ConfigManager configManager = KProfessionsCore.getConfigManager();

        // Busca a config da profissão
        YamlConfiguration config = null;
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

        if (player.getInventory().getItemInHand().getType() == Material.AIR) {
            player.sendMessage(ChatUtils.color("&cColoque o item INGREDIENTE na mão!"));
            return;
        }

        int slot;
        boolean exists = false;
        int[] craftSlots = ProfessionCraftItemLimit.getValidSlots(profissao);

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

        if (!exists || slot > 45 || slot <= 0) {
            player.sendMessage(ChatUtils.color("&cSlot inválido!"));
            player.sendMessage(ChatUtils.color("&aSlots validos: " + Arrays.toString(Arrays.stream(craftSlots).sorted().toArray())));
            return;
        }

        if (!configManager.checkIfCraftExists(craftName)) {
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

        // Salvando o item na config
        config.set("Craft." + craftName + ".ingredients." + slot + ".item", player.getInventory().getItemInHand());
        configManager.saveYaml(config, ConfigPaths.CRAFTS.getPath(), profissao + ".yml");

        player.sendMessage(ChatUtils.color("&aIngrediente adicionado no slot &e" + slot));
    }
}
package kendo.me.kproffesionscore.commands.profession.admin.subcommands;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.commands.action.CommandAction;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.crafts.CraftManager;
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

public class CraftCreate extends CommandBuilder implements CommandAction {

    public CraftCreate(JavaPlugin plugin) {
        super(plugin, "profissoes");
    }


    @Override
    public void execute(Player player, @Nullable String[] args) {

        if (args == null || args.length < 5) {
            player.sendMessage(ChatUtils.color("&cUse: /profissoes admin craft <create> <profissao> <nomeCraft>"));
            return;
        }

        // args[1] = craft
        if (!args[1].equalsIgnoreCase("craft")) {
            return;
        }

        String action = args[2].toLowerCase();     // create
        String profissao = args[3].toLowerCase(); // profissao
        String craftName = args[4]; // nome do craft

        ConfigManager configManager = KProfessionsCore.getConfigManager();
        YamlConfiguration config = null;
        CraftManager craftManager = KProfessionsCore.getCraftManager();

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
                craftManager.loadAll(); // reiniciar o map
            }
            default -> player.sendMessage(ChatUtils.color("&cAção inválida: " + action));
        }
    }
}

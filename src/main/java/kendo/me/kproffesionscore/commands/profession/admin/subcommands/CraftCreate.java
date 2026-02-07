package kendo.me.kproffesionscore.commands.profession.admin.subcommands;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.commands.action.CommandAction;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.crafts.CraftManager;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.manager.config.paths.ConfigFiles;
import kendo.me.kproffesionscore.manager.config.paths.ConfigPaths;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class CraftCreate extends CommandBuilder implements CommandAction {

    public CraftCreate(JavaPlugin plugin) {
        super(plugin, "profissoes");
    }

    @Override
    public void execute(Player player, @Nullable String[] args) {
        // Aumentamos para 7 argumentos: admin craft create <profissao> <nomeCraft> <nivel> <exp>
        if (args == null || args.length < 7) {
            player.sendMessage(ChatUtils.color("&cUse: /profissoes admin craft create <profissao> <nomeCraft> <nivelRequired> <expGain>"));
            return;
        }

        if (!args[1].equalsIgnoreCase("craft")) {
            return;
        }

        String action = args[2].toLowerCase();
        String profissao = args[3].toLowerCase();
        String craftName = args[4];

        int requiredLevel;
        double expGain;

        try {
            requiredLevel = Integer.parseInt(args[5]);
            expGain = Double.parseDouble(args[6]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatUtils.color("&cO nível deve ser inteiro e o XP deve ser um número (ex: 10.5)!"));
            return;
        }

        ConfigManager configManager = KProfessionsCore.getConfigManager();
        YamlConfiguration config = null;
        CraftManager craftManager = KProfessionsCore.getCraftManager();

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
                if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    player.sendMessage(ChatUtils.color("&cColoque o item RESULTADO do craft na mão!"));
                    return;
                }

                if (configManager.checkIfCraftExists(craftName)) {
                    player.sendMessage(ChatUtils.color("&cEsse craft já existe!"));
                    return;
                }

                // Salvando na config
                config.set("Craft." + craftName + ".result.item", player.getInventory().getItemInMainHand());
                config.set("Craft." + craftName + ".level-required", requiredLevel);
                config.set("Craft." + craftName + ".exp-gain", expGain); // ADICIONADO
                config.createSection("Craft." + craftName + ".ingredients");

                configManager.saveYaml(config, ConfigPaths.CRAFTS.getPath(), profissao + ".yml");

                player.sendMessage(ChatUtils.color("&aCraft '&f" + craftName + "&a' criado!"));
                player.sendMessage(ChatUtils.color("&7Nível: &e" + requiredLevel + " &8| &7XP: &a" + expGain));

                craftManager.loadAll();
            }
            default -> player.sendMessage(ChatUtils.color("&cAção inválida: " + action));
        }
    }
}
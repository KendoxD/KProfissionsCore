package kendo.me.kproffesionscore.commands.profession.admin.subcommands;

import kendo.me.kproffesionscore.commands.action.CommandAction;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.utils.ChatUtils;
import kendo.me.kproffesionscore.utils.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GiveCombatenteItem extends CommandBuilder implements CommandAction {

    private final ConfigUtils configUtils = new ConfigUtils();

    public GiveCombatenteItem(JavaPlugin plugin) {
        super(plugin, "profissoes");
    }

    @Override
    public void execute(Player player, @Nullable String[] args) {
        // /profissoes admin combatente give <player> <id_da_espada> [quantidade]
        if (args == null || args.length < 5) {
            player.sendMessage(ChatUtils.color("&cUse: /profissoes admin combatente give <player> <id_da_espada> [quantidade]"));
            return;
        }

        Player target = player.getServer().getPlayer(args[3]);
        if (target == null) {
            player.sendMessage(ChatUtils.color("&cJogador offline!"));
            return;
        }

        String itemId = args[4].toLowerCase();
        // Carrega o arquivo combatente.yml (onde está a Ace do Roger, etc)
        YamlConfiguration config = configUtils.getConfigFile("combatente");

        // Verifica se o ID existe na seção 'espadas' do yml
        if (!config.contains("espadas." + itemId)) {
            if (config.getConfigurationSection("espadas") != null) {
                Set<String> keys = config.getConfigurationSection("espadas").getKeys(false);
                player.sendMessage(ChatUtils.color("&cItem de combatente não encontrado! Disponíveis: &e" + String.join(", ", keys)));
            } else {
                player.sendMessage(ChatUtils.color("&cErro: Nenhuma espada configurada em combatente.yml"));
            }
            return;
        }

        String path = "espadas." + itemId + ".";

        // Busca o material (Padrão NETHERITE_SWORD se não definido)
        String matStr = config.getString(path + "item", "NETHERITE_SWORD").replace("minecraft:", "").toUpperCase();
        Material material = Material.getMaterial(matStr);
        if (material == null) {
            player.sendMessage(ChatUtils.color("&cErro: Material '" + matStr + "' inválido na config!"));
            return;
        }

        String displayName = ChatUtils.color(config.getString(path + "item-name", "&rEspada de Combatente"));
        int modelData = config.getInt(path + "model-data", 0);
        List<String> loreFromConfig = config.getStringList(path + "lore");

        int amount = 1;
        if (args.length >= 6) {
            try {
                amount = Integer.parseInt(args[5]);
            } catch (NumberFormatException ignored) {}
        }

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);

            if (modelData != 0) {
                meta.setCustomModelData(modelData);
            }

            if (!loreFromConfig.isEmpty()) {
                List<String> coloredLore = loreFromConfig.stream()
                        .map(ChatUtils::color)
                        .collect(Collectors.toList());
                meta.setLore(coloredLore);
            } else {
                List<String> defaultLore = new ArrayList<>();
                defaultLore.add(ChatUtils.color("&7Item de Profissão: &fCombatente"));
                meta.setLore(defaultLore);
            }

            item.setItemMeta(meta);
        }

        target.getInventory().addItem(item);

        player.sendMessage(ChatUtils.color("&aVocê deu &e" + amount + "x " + displayName + " &apara &f" + target.getName()));
        target.sendMessage(ChatUtils.color("&aVocê recebeu &e" + amount + "x " + displayName));
    }
}
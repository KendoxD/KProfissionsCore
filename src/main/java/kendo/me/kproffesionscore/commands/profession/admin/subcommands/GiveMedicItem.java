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

public class GiveMedicItem extends CommandBuilder implements CommandAction {

    private final ConfigUtils configUtils = new ConfigUtils();

    public GiveMedicItem(JavaPlugin plugin) {
        super(plugin, "profissoes");
    }

    @Override
    public void execute(Player player, @Nullable String[] args) {
        if (args == null || args.length < 5) {
            player.sendMessage(ChatUtils.color("&cUse: /profissoes admin medico give <player> <id_da_skill> [quantidade]"));
            return;
        }

        Player target = player.getServer().getPlayer(args[3]);
        if (target == null) {
            player.sendMessage(ChatUtils.color("&cJogador offline!"));
            return;
        }

        String skillId = args[4].toLowerCase();
        YamlConfiguration config = configUtils.getConfigFile("medico");

        if (!config.contains("skills." + skillId)) {
            Set<String> keys = config.getConfigurationSection("skills").getKeys(false);
            player.sendMessage(ChatUtils.color("&cSkill não encontrada! Disponíveis: &e" + String.join(", ", keys)));
            return;
        }

        String path = "skills." + skillId + ".";
        String matStr = config.getString(path + "item", "PAPER").replace("minecraft:", "").toUpperCase();
        Material material = Material.getMaterial(matStr);
        String displayName = ChatUtils.color(config.getString(path + "item-name", "&rItem Médico"));
        int modelData = config.getInt(path + "model-data", 0);

        int amount = 1;
        if (args.length >= 6) {
            try {
                amount = Integer.parseInt(args[5]);
            } catch (NumberFormatException ignored) {}
        }

        if (material == null) {
            player.sendMessage(ChatUtils.color("&cErro: Material '" + matStr + "' inválido na config!"));
            return;
        }
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (modelData != 0) {
                meta.setCustomModelData(modelData);
            }
            List<String> lore = new ArrayList<>();
            lore.add(ChatUtils.color("&7Item de Profissão: &fMédico"));
            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        target.getInventory().addItem(item);
        player.sendMessage(ChatUtils.color("&aVocê deu &e" + amount + "x " + displayName + " &apara &f" + target.getName()));
        target.sendMessage(ChatUtils.color("&aVocê recebeu &e" + amount + "x " + displayName));
    }
}
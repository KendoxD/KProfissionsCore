package kendo.me.kproffesionscore.commands.profession.admin.subcommands;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.commands.action.CommandAction;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.manager.config.paths.ConfigFiles;
import kendo.me.kproffesionscore.manager.config.paths.ConfigPaths;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class CraftList extends CommandBuilder implements CommandAction {
    public CraftList(JavaPlugin plugin) {
        super(plugin, "profissoes");
    }

    @Override
    public void execute(Player player, @Nullable String[] args) {
      //TODO: do this bitch
        if (args == null || args.length < 4) {
            player.sendMessage(ChatUtils.color("&cUse: /profissoes admin craft <list> <profissao>"));
            return;
        }
        ConfigManager configManager = KProfessionsCore.getConfigManager();
        String profissao = args[3].toLowerCase(); // profissao
        YamlConfiguration config = null;

        // Busca o arquivo correto da profissão
        for (ConfigFiles file : ConfigFiles.values()) {
            if (file.name().equalsIgnoreCase(profissao)) {
                config = configManager.getCraftFile(ConfigPaths.CRAFTS, file);
                break;
            }
        }
        if(config == null){
            player.sendMessage(ChatUtils.color("Profissao invalida! : " + profissao));
            return;
        }


        // Chama o método do manager e envia cada linha
        configManager.getFormattedCraftList(config, profissao)
                .forEach(player::sendMessage);
    }
}

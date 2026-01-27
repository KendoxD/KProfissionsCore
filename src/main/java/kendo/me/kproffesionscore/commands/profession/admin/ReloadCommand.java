package kendo.me.kproffesionscore.commands.profession.admin;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.manager.config.paths.ConfigPaths;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class ReloadCommand extends CommandBuilder {
    private ConfigManager configManager;

    public ReloadCommand(JavaPlugin plugin, ConfigManager configManager) {
        super(plugin, "profissoes");
        this.configManager = configManager;
        this.setDescription("Comando principal de profissões")
                .setAliases("kjobs", "profission");
    }
    public void execute(Player player, @Nullable String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cUse /profissoes admin reload");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUse /profissoes admin reload");
            return;
        }
        String secondArg = args[1].toLowerCase();
        if (secondArg.equals("reload")) {
            configManager.reloadAllCraftFiles();
            player.sendMessage("§aCrafts recarregados com sucesso!");
            return;
        }

        player.sendMessage("§cSubcomando desconhecido: " + secondArg);

    }
}


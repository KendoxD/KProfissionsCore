package kendo.me.kproffesionscore.commands.profession.admin.subcommands;
import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Sound;
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
        if(!player.hasPermission("kprofessions.admin.reload")) {
            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para realizar esta ação."));
            return;
        }
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
            configManager.reloadAll();
            configManager.reloadAllCraftFiles();
            KProfessionsCore.getCraftManager().loadAll();
            player.sendMessage(ChatUtils.color("&a&l[!] &aConfigurações e receitas de craft recarregadas!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            player.sendMessage(ChatUtils.color("§aCrafts recarregados com sucesso!"));
            return;
        }

        player.sendMessage("§cSubcomando desconhecido: " + secondArg);

    }
}


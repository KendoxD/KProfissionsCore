package kendo.me.kproffesionscore.commands.profession.admin;

import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.CraftAdd;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.CraftCreate;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.CraftList;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftCommand extends CommandBuilder {
    private final JavaPlugin plugin;
    public CraftCommand(JavaPlugin plugin) {
        super(plugin, "profissoes");
        this.plugin = plugin;
    }

    public void execute(Player player, String[] args) {
        // args[0] = admin, args[1] = craft, args[2] = ação (create/add)
        if (args.length < 3) {
            player.sendMessage(ChatUtils.color("&cUse: /profissoes admin craft <create|add|list|remove>"));
            return;
        }

        String action = args[2].toLowerCase();

        switch (action) {
            case "create" -> new CraftCreate(plugin).execute(player, args);
            case "add" -> new CraftAdd(plugin).execute(player, args);
            case "list" -> new CraftList(plugin).execute(player, args);
            default -> player.sendMessage(ChatUtils.color("&cAção de craft inválida!"));
        }
    }
}

package kendo.me.kproffesionscore.commands.builder;

import kendo.me.kproffesionscore.commands.action.CommandAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CommandBuilder {

    private final JavaPlugin plugin;
    private final String name;
    private List<String> aliases;
    private String usage = "";
    private String description = "";
    private CommandAction action;

    public CommandBuilder(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public CommandBuilder setAliases(String... aliases){
        this.aliases = Arrays.asList(aliases);
        return this;
    }

    public CommandBuilder setUsage(String usage) {
        this.usage = usage;
        return this;
    }
    public CommandBuilder setDescription(String description) {
        this.description = description;
        return this;
    }
    public CommandBuilder setAction(CommandAction action) {
        this.action = action;
        return this;
    }


    public void register(){
        PluginCommand command = plugin.getCommand(name);
        if(command == null) {
            System.out.println("Comando nao registrado no plugin.yml, " + name);
            return;
        }
        command.setAliases(aliases);
        command.setDescription(description);
        command.setUsage(usage);
        command.setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("[KProfissionsCore]Only players can execute this command!");
                    return true;
                }
                if (action != null) {
                    action.execute((Player) sender, args);
                }
                return true;
            }
        });
    }


}

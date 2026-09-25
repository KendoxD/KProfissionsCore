package kendo.me.kproffesionscore.commands.profession.admin.subcommands;

import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.commands.profession.admin.CraftCommand;
import kendo.me.kproffesionscore.manager.config.ConfigManager;
import kendo.me.kproffesionscore.professions.database.connection.ProfissionDatabase;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class ProfessionAdminCommands extends CommandBuilder {

    private final ConfigManager configManager;
    private final ProfissionDatabase database;
    private final JavaPlugin plugin;

    public ProfessionAdminCommands(JavaPlugin plugin, ConfigManager configManager, ProfissionDatabase database) {
        super(plugin, "profissoes-admin"); // nome interno, não é registado diretamente como comando
        this.plugin = plugin;
        this.configManager = configManager;
        this.database = database;
        this.setDescription("Comandos administrativos de profissões")
                .setAliases("kjobs", "profission");
    }

    @Override
    public void execute(Player player, @Nullable String[] args) {
        if (!player.hasPermission("kprofessions.admin")) {
            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para usar os comandos de administrador!"));
            return;
        }

        if (args == null || args.length < 2) {
            player.sendMessage(ChatUtils.color("&cUse /profissoes admin <reload|craft|profissao|combatente|medico>"));
            return;
        }

        String adminSub = args[1].toLowerCase();

        switch (adminSub) {
            case "reload" -> handleReload(player, args);
            case "craft" -> handleCraft(player, args);
            case "combatente" -> handleGiveCombatente(player, args);
            case "medico" -> handleGiveMedico(player, args);
            case "profissao" -> handleProfissao(player, args);
            default -> player.sendMessage(ChatUtils.color("&cSubcomando admin inválido: " + adminSub));
        }
    }

    private void handleReload(Player player, String[] args) {
        if (!player.hasPermission("kprofessions.admin.reload")) {
            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para recarregar as configurações."));
            return;
        }
        new ReloadCommand(plugin, configManager).execute(player, args);
    }

    private void handleCraft(Player player, String[] args) {
        if (!player.hasPermission("kprofessions.admin.craft")) {
            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para usar o comando de craft."));
            return;
        }
        new CraftCommand(plugin).execute(player, args);
    }

    private void handleGiveCombatente(Player player, String[] args) {
        if (!player.hasPermission("kprofessions.admin.give.combatente")) {
            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para dar itens de combatente."));
            return;
        }
        new GiveCombatenteItem(plugin).execute(player, args);
    }


    private void handleGiveMedico(Player player, String[] args) {
        if (!player.hasPermission("kprofessions.admin.give.medico")) {
            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para dar itens de médico."));
            return;
        }
        new GiveMedicItem(plugin).execute(player, args);
    }

    //profissao <add|remove|setlevel|addxp> <player> [args]
    private void handleProfissao(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatUtils.color("§cUse /profissoes admin profissao <add|remove|setlevel|addxp> <player> [args]"));
            return;
        }

        String action = args[2].toLowerCase();

        if (args.length < 4) {
            player.sendMessage(ChatUtils.color("§cFalta o nome do jogador. Use /profissoes admin profissao " + action + " <player>"));
            return;
        }

        String targetName = args[3];
        Player pTarget = Bukkit.getPlayer(targetName);
        String nick = (pTarget != null) ? pTarget.getName() : targetName;

        switch (action) {
            case "remove" -> removeProfession(player, pTarget, nick);
            case "add" -> {
                if (args.length < 5) {
                    player.sendMessage(ChatUtils.color("§cUse /profissoes admin profissao add <player> <medico|cozinheiro|combatente>"));
                    return;
                }
                addProfession(player, pTarget, nick, args[4]);
            }
            case "setlevel" -> {
                if (args.length < 6) {
                    player.sendMessage(ChatUtils.color("§cUse /profissoes admin profissao setlevel <player> <profissao> <nivel>"));
                    return;
                }
                setLevel(player, pTarget, nick, args[4], args[5]);
            }
            case "addxp" -> {
                if (args.length < 6) {
                    player.sendMessage(ChatUtils.color("§cUse /profissoes admin profissao addxp <player> <profissao> <quantidade>"));
                    return;
                }
                addXp(player, pTarget, nick, args[4], args[5]);
            }
            default -> player.sendMessage(ChatUtils.color("§cAção inválida: " + action + ". Use add, remove, setlevel ou addxp."));
        }
    }

    private void removeProfession(Player player, @Nullable Player pTarget, String nick) {
        if (!player.hasPermission("kprofissions.admin.remove.profission")) {
            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para remover profissões."));
            return;
        }
        if (!database.playerExists(nick)) {
            player.sendMessage(ChatUtils.color("&cEsse jogador não possui nenhuma profissão."));
            return;
        }

        String profession = database.getPlayerProfession(nick);
        boolean removed = database.removeProfession(nick, profession);

        if (removed) {
            player.sendMessage(ChatUtils.color("&aProfissão de &f" + nick + " &aremovida com sucesso (&f" + profession + "&a)."));
            if (pTarget != null && pTarget.isOnline()) {
                pTarget.sendMessage(ChatUtils.color("&cA sua profissão foi removida por um administrador."));
            }
        } else {
            player.sendMessage(ChatUtils.color("&cOcorreu um erro ao remover a profissão."));
        }
    }

    private void addProfession(Player player, @Nullable Player pTarget, String nick, String profession) {
        if (!player.hasPermission("kprofissions.admin.add.profission")) {
            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para atribuir profissões."));
            return;
        }
        if (database.playerExists(nick)) {
            player.sendMessage(ChatUtils.color("&cEsse jogador já possui uma profissão (&f" + database.getPlayerProfession(nick) + "&c)."));
            return;
        }

        boolean assigned = database.assignProfession(nick, profession);
        if (assigned) {
            player.sendMessage(ChatUtils.color("&aProfissão &f" + profession + " &aatribuída a &f" + nick + "&a."));
            if (pTarget != null && pTarget.isOnline()) {
                pTarget.sendMessage(ChatUtils.color("&aVocê recebeu a profissão &f" + profession + " &ade um administrador!"));
            }
        } else {
            player.sendMessage(ChatUtils.color("&cProfissão inválida ou erro ao atribuir: " + profession));
        }
    }

    private void setLevel(Player player, @Nullable Player pTarget, String nick, String profession, String levelStr) {
        if (!player.hasPermission("kprofissions.admin.setlevel.profission")) {
            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para definir níveis."));
            return;
        }
        int level;
        try {
            level = Integer.parseInt(levelStr);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatUtils.color("&cNível inválido: " + levelStr));
            return;
        }

        boolean updated = database.setPlayerLevel(nick, profession, level);
        if (updated) {
            player.sendMessage(ChatUtils.color("&aNível de &f" + nick + " &aem &f" + profession + " &adefinido para &f" + level + "&a."));
            if (pTarget != null && pTarget.isOnline()) {
                pTarget.sendMessage(ChatUtils.color("&aSeu nível em &f" + profession + " &afoi alterado para &f" + level + "&a."));
            }
        } else {
            player.sendMessage(ChatUtils.color("&cOcorreu um erro ao definir o nível (verifique se o jogador tem essa profissão)."));
        }
    }

    private void addXp(Player player, @Nullable Player pTarget, String nick, String profession, String amountStr) {
        if (!player.hasPermission("kprofissions.admin.addxp.profission")) {
            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para adicionar XP."));
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatUtils.color("&cQuantidade de XP inválida: " + amountStr));
            return;
        }

        boolean updated = database.addXp(nick, profession, amount);
        if (updated) {
            player.sendMessage(ChatUtils.color("&aAdicionado &f" + amount + " &aXP para &f" + nick + " &aem &f" + profession + "&a."));
            if (pTarget != null && pTarget.isOnline()) {
                pTarget.sendMessage(ChatUtils.color("&aVocê recebeu &f" + amount + " &aXP em &f" + profession + "&a!"));
            }
        } else {
            player.sendMessage(ChatUtils.color("&cOcorreu um erro ao adicionar XP (verifique se o jogador tem essa profissão)."));
        }
    }
}
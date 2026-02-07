package kendo.me.kproffesionscore.commands.profession;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.menu.Menu;
import kendo.me.kproffesionscore.builder.menu.enums.MenuType;
import kendo.me.kproffesionscore.builder.menu.handlers.MenuHandler;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.commands.profession.admin.CraftCommand;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.GiveMedicItem;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.ReloadCommand;
import kendo.me.kproffesionscore.professions.Medico;
import kendo.me.kproffesionscore.professions.database.connection.ProfissionDatabase;
import kendo.me.kproffesionscore.professions.database.connection.dao.MedicoDao;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;
import java.util.Set;

public class ProfessionCommand extends CommandBuilder {
    public ProfessionCommand(JavaPlugin plugin, MenuHandler menuHandler){
        super(plugin, "profissoes");
        this.setDescription("Abre o menu de profissoes ou gerencia sua conta!")
                .setAliases("kjobs", "profission")
                .setAction(((player, args) -> {
                    ProfissionDatabase dbManager = KProfessionsCore.getDatabase();
                    if (dbManager == null) {
                        player.sendMessage(ChatUtils.color("&c[Erro] O sistema de banco de dados não inicializou corretamente."));
                        return;
                    }

                    // --- CASO 0: /profissoes (Abre o menu ou Escolha) ---
                    if(args.length == 0) {
                        handleMenuOpening(player, dbManager, menuHandler);
                        return;
                    }

                    String sub = args[0].toLowerCase();

                    if (sub.equals("perfil")) {
                        new ProfessionPerfilCommand(plugin).execute(player, args);
                        return;
                    }

                    if (sub.equals("admin")) {
                        if (!player.hasPermission("kprofessions.admin")) {
                            player.sendMessage(ChatUtils.color("&cVocê não tem permissão para usar os comandos de administrador!"));
                            return;
                        }

                        if (args.length < 2) {
                            player.sendMessage(ChatUtils.color("&cUse /profissoes admin <reload|craft|medico>"));
                            return;
                        }

                        String adminSub = args[1].toLowerCase();
                        switch(adminSub) {
                            case "medico" -> new GiveMedicItem(plugin).execute(player, args);
                            case "reload" -> new ReloadCommand(plugin, KProfessionsCore.getConfigManager()).execute(player, args);
                            case "craft" -> new CraftCommand(plugin).execute(player, args);
                            default -> player.sendMessage(ChatUtils.color("&cSubcomando admin inválido: " + adminSub));
                        }
                        return;
                    }

                    player.sendMessage(ChatUtils.color("&cSubcomando desconhecido. Use /profissoes perfil ou /profissoes admin"));

                })).register();
    }

    /**
     * Abre o menu de craft ou de escolha ao utilizar /profissoes
     */
    private void handleMenuOpening(org.bukkit.entity.Player player, ProfissionDatabase dbManager, MenuHandler menuHandler) {
        Menu menu;
        if(dbManager.playerExists(player.getDisplayName())){
            String profission = dbManager.getPlayerProfession(player.getDisplayName());
            if(Objects.equals(profission, "medico")){
                menu = new Menu(player, MenuType.MENU_MEDICO);
                menuHandler.openMenu(menu);
            } else {
                player.sendMessage(ChatUtils.color("&e[!] Menu para sua profissão ainda não implementado."));
            }
        } else {
            menu = new Menu(player, MenuType.MENU_CHOOSE);
            menuHandler.openMenu(menu);
            menu.setOnClick(((p, slot) -> {
                Set<Integer> slotsMedico = Set.of(4, 5, 6, 13, 14, 15, 22, 23, 24);
                if (slotsMedico.contains(slot)) {
                    MedicoDao medicoDao = new MedicoDao(dbManager.getConnection());
                    Medico medic = new Medico(player.getDisplayName(), 1, 0, 1, 1, 1, 1);
                    medicoDao.save(medic);
                    menuHandler.closeMenu(menu);
                    player.sendTitle(ChatUtils.color("&aMédico Escolhido!"), ChatUtils.color("&7Use /profissoes para começar"), 10, 40, 10);
                } else {
                    player.sendMessage(ChatUtils.color("&cEsta profissão ainda está em desenvolvimento!"));
                }
            }));
        }
    }
}
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
import kendo.me.kproffesionscore.utils.skript.SkriptUtils;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;
import java.util.Set;

public class ProfessionCommand extends CommandBuilder {
    public ProfessionCommand(JavaPlugin plugin, MenuHandler menuHandler){
        super(plugin, "profissoes");
        this.setDescription("Abre o menu de profissoes!")
                .setAliases("kjobs", "profission")
                .setAction(((player, args) ->{
                    ProfissionDatabase dbManager = KProfessionsCore.getDatabase();
                    Menu menu;

                    if(args.length == 0) {
                        if(dbManager.playerExists(player.getDisplayName())){
                            SkriptUtils.setVariable(player, "vit", 200);
                            SkriptUtils.setVariable(player, "str", 300);
                            String profission = dbManager.getPlayerProfession(player.getDisplayName());
                            if(Objects.equals(profission, "medico")){
                                menu = new Menu(player, MenuType.MENU_MEDICO);
                                menuHandler.openMenu(menu);
                            }
                        } else {
                            menu = new Menu(player, MenuType.MENU_CHOOSE);
                            menuHandler.openMenu(menu);
                            menu.setOnClick(((p, slot) -> {
                                Set<Integer> slotsMedico = Set.of(4, 5, 6, 13, 14, 15, 22, 23, 24);
                                Set<Integer> slotsCombatente = Set.of(27, 28, 29, 36, 37, 38);
                                Set<Integer> slotsCozinheiro = Set.of(33, 34, 35, 42, 43, 44);

                                if (slotsMedico.contains(slot)) {
                                    MedicoDao medicoDao = new MedicoDao(dbManager.getConnection());
                                    Medico medic = new Medico(player.getDisplayName(), 1, 0, 1, 1, 1, 1);
                                    medicoDao.save(medic);
                                    menuHandler.closeMenu(menu);
                                    player.sendTitle(ChatUtils.color("&aVocê escolheu a profissão: Médico!"), "", 2,10,20
                                    );
                                }
                                if (slotsCombatente.contains(slot)) {
                                    player.sendMessage(ChatUtils.color(
                                            "&c[Professions] Parabéns, você escolheu a profissão: Combatente !"
                                    ));
                                }
                                if (slotsCozinheiro.contains(slot)) {
                                    player.sendMessage(ChatUtils.color(
                                            "&c[Professions] Parabéns, você escolheu a profissão: Cozinheiro !"
                                    ));
                                }
                                ;
                            }));
                        }
                        return;
                    }

                    if(!player.isOp()) {
                        player.sendMessage(ChatUtils.color("&cVocê não tem permissão para usar subcomandos!"));
                        return;
                    }

                    // args[0] é o "admin"
                    if(args[0] != null&& !args[0].equalsIgnoreCase("admin")) {
                        player.sendMessage(ChatUtils.color("&cSubcomando inválido: " + args[0]));
                        return;
                    }

                    if(args.length < 2) {
                        player.sendMessage(ChatUtils.color("&cUse /profissoes admin <subcomando>"));
                        return;
                    }
                    if(args[1]== null) return;
                    String subCommand = args[1].toLowerCase();

                    switch(subCommand) {
                        case "medico" -> new GiveMedicItem(plugin).execute(player, args);
                        case "reload" -> new ReloadCommand(plugin, KProfessionsCore.getConfigManager()).execute(player, args);
                        case "craft" -> new CraftCommand(plugin).execute(player, args);
                        default -> player.sendMessage(ChatUtils.color("&Subcomando admin inválido: " + subCommand));
                    }
                })).register();
    }

}

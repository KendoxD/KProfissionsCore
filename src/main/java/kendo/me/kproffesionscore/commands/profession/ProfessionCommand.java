package kendo.me.kproffesionscore.commands.profession;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.item.ItemBuilder;
import kendo.me.kproffesionscore.builder.menu.Menu;
import kendo.me.kproffesionscore.builder.menu.enums.MenuType;
import kendo.me.kproffesionscore.builder.menu.handlers.MenuHandler;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.professions.Medico;
import kendo.me.kproffesionscore.professions.PlayerProfession;
import kendo.me.kproffesionscore.professions.database.connection.ProfissionDatabase;
import kendo.me.kproffesionscore.professions.database.connection.dao.MedicoDao;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Set;

public class ProfessionCommand extends CommandBuilder {
    public ProfessionCommand(JavaPlugin plugin, MenuHandler menuHandler){
        super(plugin, "profissoes");
        this.setDescription("Abre o menu de profissoes!")
                .setAliases("profmenu", "menuprofissao")
                .setAction(((player, args) ->{
                    ProfissionDatabase dbManager = KProfessionsCore.getDatabase();
                    Menu menu;
                    //30000 - item blank
                    ItemBuilder item = new ItemBuilder(Material.PAPER)
                            .setName(ChatUtils.color("&c-"))
                            .setModelData(30000);
                    if(dbManager.playerExists(player.getDisplayName())){
                        String profission = dbManager.getPlayerProfession(player.getDisplayName());
                        if(Objects.equals(profission, "medico")){
                            menu = new Menu(player, MenuType.MENU_MEDICO);
                            menu.fullFillInventory(item.build());
                            menuHandler.openMenu(menu);
                        }
                    } else {
                        menu = new Menu(player, MenuType.MENU_CHOOSE);
                        menu.fullFillInventory(item.build());
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
                        player.sendMessage(ChatUtils.color("&a[Professions] Menu opened successfully!"));
                    }
                })).register();
    }
}

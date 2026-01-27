package kendo.me.kproffesionscore.commands.profession;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.menu.Menu;
import kendo.me.kproffesionscore.builder.menu.enums.MenuType;
import kendo.me.kproffesionscore.builder.menu.handlers.MenuHandler;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.commands.profession.admin.CraftCreate;
import kendo.me.kproffesionscore.commands.profession.admin.ReloadCommand;
import kendo.me.kproffesionscore.professions.database.connection.ProfissionDatabase;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

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
                            String profission = dbManager.getPlayerProfession(player.getDisplayName());
                            if(Objects.equals(profission, "medico")){
                                menu = new Menu(player, MenuType.MENU_MEDICO);
                                menu.fullFillInventory();
                                menuHandler.openMenu(menu);
                            }
                        } else {
                            menuHandler.openMenu(new Menu(player, MenuType.MENU_CHOOSE));
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
                        case "reload" -> new ReloadCommand(plugin, KProfessionsCore.getConfigManager()).execute(player, args);
                        case "craft" -> new CraftCreate(plugin).execute(player, args);
                        default -> player.sendMessage(ChatUtils.color("&Subcomando admin inválido: " + subCommand));
                    }
                })).register();
    }
}

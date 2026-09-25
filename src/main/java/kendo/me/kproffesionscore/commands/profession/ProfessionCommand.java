package kendo.me.kproffesionscore.commands.profession;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.menu.Menu;
import kendo.me.kproffesionscore.builder.menu.enums.MenuType;
import kendo.me.kproffesionscore.builder.menu.handlers.MenuHandler;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.commands.profession.admin.CraftCommand;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.GiveCombatenteItem;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.GiveMedicItem;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.ReloadCommand;
import kendo.me.kproffesionscore.commands.profession.admin.subcommands.ProfessionAdminCommands;
import kendo.me.kproffesionscore.professions.Combatente;
import kendo.me.kproffesionscore.professions.Cozinheiro;
import kendo.me.kproffesionscore.professions.Medico;
import kendo.me.kproffesionscore.professions.database.connection.ProfissionDatabase;
import kendo.me.kproffesionscore.professions.database.connection.dao.CombatenteDao;
import kendo.me.kproffesionscore.professions.database.connection.dao.CozinheiroDao;
import kendo.me.kproffesionscore.professions.database.connection.dao.MedicoDao;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class ProfessionCommand extends CommandBuilder {

    private final JavaPlugin plugin;

    public ProfessionCommand(JavaPlugin plugin, MenuHandler menuHandler){
        super(plugin, "profissoes");
        this.plugin = plugin;
        this.setDescription("Abre o menu de profissoes ou gerencia sua conta!")
                .setAliases("kjobs", "profission")
                .setAction(((player, args) -> {
                    ProfissionDatabase dbManager = KProfessionsCore.getDatabase();
                    if (dbManager == null) {
                        player.sendMessage(ChatUtils.color("&c[Erro] O sistema de banco de dados não inicializou corretamente."));
                        return;
                    }

                    // CASO 0: /profissoes
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
                            player.sendMessage(ChatUtils.color("&cUse /profissoes admin <reload|craft|profissao>"));
                            return;
                        }


                        new ProfessionAdminCommands(plugin, KProfessionsCore.getConfigManager(), dbManager).execute(player, args);
                        return;
                    }

                    player.sendMessage(ChatUtils.color("&cSubcomando desconhecido. Use /profissoes perfil ou /profissoes admin"));

                })).register();
    }

    /**
     * Abre o menu de craft ou de escolha ao utilizar /profissoes
     */
    private void handleMenuOpening(Player player, ProfissionDatabase dbManager, MenuHandler menuHandler) {
        Menu menu;
        if(dbManager.playerExists(player.getDisplayName())){
            String profission = dbManager.getPlayerProfession(player.getDisplayName());

            if(Objects.equals(profission, "medico")){
                menu = new Menu(player, MenuType.MENU_MEDICO);
                menuHandler.openMenu(menu);
            } else if (Objects.equals(profission, "combatente")) {
                menu = new Menu(player, MenuType.MENU_FORJA);
                menuHandler.openMenu(menu);
            } else {
                player.sendMessage(ChatUtils.color("&e[!] Menu para sua profissão ainda não implementado."));
            }
        } else {
            menu = new Menu(player, MenuType.MENU_CHOOSE);
            menuHandler.openMenu(menu);
            menu.setOnClick(((p, slot) -> {
                Set<Integer> slotsMedico = Set.of(4, 5, 6, 13, 14, 15, 22, 23, 24);
                Set<Integer> slotsCombatente = Set.of(27, 28, 29, 36, 37, 38);;

                if (slotsMedico.contains(slot)) {
                    MedicoDao medicoDao = new MedicoDao(dbManager.getConnection());
                    Medico medic = new Medico(player.getDisplayName(), 1, 0, 1, 1, 1, 1);
                    medicoDao.save(medic);
                    menuHandler.closeMenu(menu);
                    player.sendTitle(
                            ChatUtils.color("&aVocê escolheu a profissão: Médico!"),
                            ChatUtils.color("&7Use /profissoes para começar"),
                            10, 40, 10
                    );
                    spawnMedicoParticles(player);
                } else if (slotsCombatente.contains(slot)) {
                    CombatenteDao combatenteDao = new CombatenteDao(dbManager.getConnection());
                    Combatente combatente = new Combatente(player.getDisplayName(), 1, 0, 1, 1, 1);
                    combatenteDao.save(combatente);
                    menuHandler.closeMenu(menu);
                    player.sendTitle(
                            ChatUtils.color("&aVocê escolheu a profissão: Combatente!"),
                            ChatUtils.color("&7Use /profissoes para começar"),
                            10, 40, 10
                    );
                    spawnCombatenteParticles(player);
                } else {
                    player.sendMessage(ChatUtils.color("&cEsta profissão ainda está em desenvolvimento!"));
                }
            }));
        }
    }

    // =========================================================
    //  Animações de partículas por profissão
    // =========================================================

    private void spawnMedicoParticles(Player player) {
        Color[] palette = {
                Color.fromRGB(148, 0, 211),
                Color.fromRGB(186, 85, 211),
                Color.fromRGB(221, 160, 221)
        };
        playSpiralAnimation(player, palette, Particle.HEART, 6);
    }

    private void spawnCombatenteParticles(Player player) {
        Color[] palette = {
                Color.fromRGB(255, 0, 0),
                Color.fromRGB(255, 69, 0),
                Color.fromRGB(139, 0, 0)
        };
        playSpiralAnimation(player, palette, Particle.CRIT, 10);
    }

    private void spawnCozinheiroParticles(Player player) {
        Color[] palette = {
                Color.fromRGB(255, 215, 0),
                Color.fromRGB(210, 180, 140),
                Color.fromRGB(139, 69, 19)
        };
        playSpiralAnimation(player, palette, Particle.FLAME, 3);
    }

    private void playSpiralAnimation(Player player, Color[] palette, Particle accent, int accentEveryTicks) {
        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 50;

            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                Location base = player.getLocation().add(0, 0.2, 0);
                double radius = 1.1;
                double angle = Math.toRadians(ticks * 24);
                double height = (ticks / (double) duration) * 2.2;

                for (int i = 0; i < 2; i++) {
                    double a = angle + (i * Math.PI);
                    double x = radius * Math.cos(a);
                    double z = radius * Math.sin(a);
                    Location dustLoc = base.clone().add(x, height, z);

                    Color color = palette[(ticks + i) % palette.length];
                    Particle.DustOptions dust = new Particle.DustOptions(color, 1.3f);
                    player.getWorld().spawnParticle(Particle.REDSTONE, dustLoc, 1, 0, 0, 0, 0, dust);
                }

                if (ticks % accentEveryTicks == 0) {
                    Location accentLoc = base.clone().add(0, height, 0);
                    player.getWorld().spawnParticle(accent, accentLoc, 3, 0.4, 0.2, 0.4, 0.01);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void execute(Player player, @Nullable String[] args) {

    }
}
package kendo.me.kproffesionscore.commands.profession;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.commands.action.CommandAction;
import kendo.me.kproffesionscore.commands.builder.CommandBuilder;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class ProfessionPerfilCommand extends CommandBuilder implements CommandAction {

    public ProfessionPerfilCommand(JavaPlugin plugin) {
        super(plugin, "profissoes");
    }
    @Override
    public void execute(Player player, @Nullable String[] args) {
            var db = KProfessionsCore.getDatabase();
            String profession = db.getPlayerProfession(player.getDisplayName());

            if (profession == null) {
                player.sendMessage(ChatUtils.color("&cVocê ainda não possui uma profissão registrada!"));
                return;
            }

            int level = db.getPlayerLevel(player.getDisplayName(), profession);
            double currentXp = 0;

            if (profession.equalsIgnoreCase("medico")) {
                var dao = new kendo.me.kproffesionscore.professions.database.connection.dao.MedicoDao(db.getConnection());
                var medico = dao.load(player.getDisplayName());
                if (medico != null) currentXp = medico.getProfissionXp();
            }
            // Adicionar as proximas profissoes aqui depois
            double nextLevelXp = level * 100.0;
            String progressBar = getProgressBar(currentXp, nextLevelXp);

            player.sendMessage("");
            player.sendMessage(ChatUtils.color(" &6&lSTATUS DE PROFISSÃO"));
            player.sendMessage(ChatUtils.color(" &fProfissão: &e" + capitalize(profession)));
            player.sendMessage(ChatUtils.color(" &fNível Atual: &b" + level));
            player.sendMessage(ChatUtils.color(" &fExperiência: &7[" + progressBar + "&7] &8(" + String.format("%.1f", currentXp) + "/" + nextLevelXp + ")"));
            player.sendMessage("");
        }

    /**
     * Gera a barra de progresso visual
     */
    private String getProgressBar(double current, double max) {
        int totalBars = 20;
        double percentage = Math.min(1.0, current / max); // Garante que não passe de 100% visualmente
        int progressBars = (int) (totalBars * percentage);
        int leftOver = totalBars - progressBars;

        return "&a" + "|".repeat(Math.max(0, progressBars)) +
                "&8" + "|".repeat(Math.max(0, leftOver));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
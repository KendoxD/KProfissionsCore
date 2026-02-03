package kendo.me.kproffesionscore.utils.skript;

import ch.njol.skript.variables.Variables;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SkriptUtils {

    /**
     * Obtém o valor de uma variável do Skript associada ao jogador.
     * No formato {nome.%player%}.
     */
    public static double getSkriptVariable(@NotNull Player player, @NotNull String variableName) {
        Object var = Variables.getVariable(variableName + "." + player.getName(), null, false);
        if (var instanceof Number) {
            return ((Number) var).doubleValue();
        }
        return 0.0;
    }

    public static void setVariable(@NotNull Player player, @NotNull String varName, double value) {
        Variables.setVariable(varName + "." + player.getName(), value, null, false);
    }


    /**
     * Calcula a forca final com modos / formas
     * @param player
     * @return o str final do player
     */
    public static double getFinalStrength(@NotNull Player player) {
        double str = getSkriptVariable(player, "str"); //
        double modosStr = getSkriptVariable(player, "modos.str"); //
        double formasStr = getSkriptVariable(player, "formas.str"); //

        // Se formas.str não estiver setado
        if (formasStr <= 0) formasStr = 1;

        return (str + modosStr) * formasStr;
    }

    /**
     *  Obtém a Vitalidade Final para cálculos de HP.
     *  Fórmula: (vit + modos.vit) * formas.vit
     *  @param player
     */
    public static double getFinalVitality(@NotNull Player player) {
        double vit = getSkriptVariable(player, "vit"); //
        double modosVit = getSkriptVariable(player, "modos.vit"); //
        double formasVit = getSkriptVariable(player, "formas.vit"); //

        if (formasVit <= 0) formasVit = 1;

        return (vit + modosVit) * formasVit; //
    }
}
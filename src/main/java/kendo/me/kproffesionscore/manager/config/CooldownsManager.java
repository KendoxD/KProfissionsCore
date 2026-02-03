package kendo.me.kproffesionscore.manager.config;

import kendo.me.kproffesionscore.utils.ChatUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownsManager {

    private final Map<String, Long> cooldowns = new HashMap<>();

    /**
     * Define o cooldown para um jogador e uma skill específica.
     * @param uuid UUID do jogador
     * @param skillId ID da skill vindo da config (ex: "seringa-verde")
     * @param seconds Tempo em segundos
     */
    public void setCooldown(UUID uuid, String skillId, int seconds) {
        long expireTime = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.put(uuid.toString() + ":" + skillId, expireTime);
    }

    /**
     * Verifica se o player ainda esta em cooldown naquela skill
     * @param uuid
     * @param skillId
     * @return boolean se esta dentro do cooldown ainda ou nao
     */
    public boolean isCooldown(UUID uuid, String skillId) {
        String key = uuid.toString() + ":" + skillId;
        return cooldowns.containsKey(key) && cooldowns.get(key) > System.currentTimeMillis();
    }

    /**
     * Retorna o tempo restante em segundos.
     * @param uuid
     * @param skillId
     * @return tempo restante em segundos
     */
    public double getRemaining(UUID uuid, String skillId) {
        String key = uuid.toString() + ":" + skillId;
        if (!cooldowns.containsKey(key)) return 0;

        long remaining = cooldowns.get(key) - System.currentTimeMillis();
        return Math.max(0, remaining / 1000.0);
    }

    /**
     * Limpa cooldowns expirados para evitar acúmulo na memória.
     */
    public void cleanExpired() {
        cooldowns.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
    }

    /**
     * Utilizado pra barra de cooldown que sera enviada pro action bar
     * @param remaining Tempo restante
     * @param total Tempo total de cooldown
     * @param bars Quantidade de pauzinhos na barra
     * @param symbol O caractere usado (Ex: "|")
     * @param completedColor Cor do que já carregou
     * @param leftColor Cor do que falta carregar
     */
    public String getProgressBar(double remaining, int total, int bars, String symbol, String completedColor, String leftColor) {
        double percentage = (total - remaining) / total;
        int completedBars = (int) (bars * percentage);

        StringBuilder bar = new StringBuilder(ChatUtils.color(completedColor));
        for (int i = 0; i < bars; i++) {
            if (i == completedBars) bar.append(ChatUtils.color(leftColor));
            bar.append(symbol);
        }
        return ChatUtils.color(bar.toString());
    }
}
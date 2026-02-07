package kendo.me.kproffesionscore.professions;

import org.bukkit.entity.Player;

public class Medico {

    private String nick;
    private int profissionLevel;
    private double profissionXp;
    private double mastery;
    private int healLevel;
    private int medicinePower;
    private int medicalMastery;

    public Medico(String nick, int profissionLevel, double profissionXp, double mastery, int healLevel, int medicinePower, int medicalMastery) {
        this.nick = nick;
        this.profissionLevel = profissionLevel;
        this.profissionXp = profissionXp;
        this.mastery = mastery;
        this.healLevel = healLevel;
        this.medicinePower = medicinePower;
        this.medicalMastery = medicalMastery;
    }

    /**
     * Adiciona XP ao médico e gerencia o sistema de Level Up.
     * @param amount Quantidade de XP a ser adicionada.
     */
    public void addExp(double amount) {
        this.profissionXp += amount;
        while (this.profissionXp >= getXpToNextLevel()) {
            this.profissionXp -= getXpToNextLevel();
            this.profissionLevel++;

            Player player = org.bukkit.Bukkit.getPlayer(this.nick);
            if (player != null && player.isOnline()) {
                player.sendMessage("");
                player.sendMessage(" §a§l↑ NÍVEL DE PROFISSÃO AUMENTADO! ↑");
                player.sendMessage(" §fSua habilidade como §6Médico §fsubiu para o nível §e" + this.profissionLevel + "§f!");
                player.sendMessage("");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            }
        }
    }

    /**
     * Define a curva de dificuldade para subir de nível.
     * @return XP necessário para o próximo nível.
     */
    public double getXpToNextLevel() {
        return this.profissionLevel * 100.0;
    }

    // --- Getters e Setters ---
    public String getNick() { return nick; }
    public int getProfissionLevel() { return profissionLevel; }
    public double getProfissionXp() { return profissionXp; }
    public double getMastery() { return mastery; }
    public int getHealLevel() { return healLevel; }
    public int getMedicinePower() { return medicinePower; }
    public int getMedicalMastery() { return medicalMastery; }
}
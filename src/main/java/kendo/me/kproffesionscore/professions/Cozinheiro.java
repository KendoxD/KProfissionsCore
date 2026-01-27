package kendo.me.kproffesionscore.professions;

import org.bukkit.entity.Player;

public class Cozinheiro extends PlayerProfession {
    private int healLevel;
    private int medicinePower;

    public Cozinheiro(String nick, int profissionLevel, double profissionXp, double mastery, int healLevel, int medicinePower) {
        super(nick, profissionLevel, profissionXp, mastery);
        this.healLevel = healLevel;
        this.medicinePower = medicinePower;
    }

    public int getHealLevel() {
        return healLevel;
    }

    public int getMedicinePower() {
        return medicinePower;
    }

    public void setMedicinePower(int medicinePower) {
        this.medicinePower = medicinePower;
    }

    public void setHealLevel(int healLevel) {
        this.healLevel = healLevel;
    }
}

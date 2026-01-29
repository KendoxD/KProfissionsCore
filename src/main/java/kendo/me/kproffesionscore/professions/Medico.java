package kendo.me.kproffesionscore.professions;

import org.bukkit.entity.Player;

public class Medico extends PlayerProfession {
    private int healLevel;
    private int medicinePower;
    private int medicalMastery;

    public Medico(String nick, int profissionLevel, double profissionXp, double mastery, int healLevel, int medicinePower, int medicalMastery) {
        super(nick, profissionLevel, profissionXp, mastery);
        this.healLevel = healLevel; // depende do nivel dos outros dois?
        this.medicinePower = medicinePower; // crafts por raridade + cura a si mesmo ou players - da xp
        this.medicalMastery = medicalMastery; // cura apenas - da xp
    }


    public int getHealLevel() {
        return healLevel;
    }

    public int getMedicinePower() {
        return medicinePower;
    }

    public int getMedicalMastery() {
        return medicalMastery;
    }

    public void setMedicalMastery(int medicalMastery) {
        this.medicalMastery = medicalMastery;
    }

    public void setMedicinePower(int medicinePower) {
        this.medicinePower = medicinePower;
    }

    public void setHealLevel(int healLevel) {
        this.healLevel = healLevel;
    }
}

package kendo.me.kproffesionscore.professions;

import org.bukkit.entity.Player;

public class Combatente extends  PlayerProfession{
    private int forgelevel;
    private double masterySword;

    public Combatente(String nick, int profissionLevel, double profissionXp, double mastery, int forgelevel, double masterySword) {
        super(nick, profissionLevel, profissionXp, mastery);
        this.forgelevel = forgelevel;
        this.masterySword = masterySword;
    }


    public int getForgelevel() {
        return forgelevel;
    }

    public double getMasterySword() {
        return masterySword;
    }

    public void setMasterySword(double masterySword) {
        this.masterySword = masterySword;
    }

    public void setForgelevel(int forgelevel) {
        this.forgelevel = forgelevel;
    }
}

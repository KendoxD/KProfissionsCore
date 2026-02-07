package kendo.me.kproffesionscore.professions;


public abstract class PlayerProfession {
    private String nick;
    private int profissionLevel;
    private double profissionXp;
    private double mastery;

    public PlayerProfession(String nick,int profissionLevel, double profissionXp, double mastery){
        this.nick = nick;
        this.profissionLevel = profissionLevel;
        this.mastery = mastery;
        this.profissionXp= profissionXp;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getProfissionLevel() {
        return profissionLevel;
    }

    public double getMastery() {
        return mastery;
    }

    public void setMastery(double mastery) {
        this.mastery = mastery;
    }

    public double getProfissionXp() {
        return profissionXp;
    }

    public void setProfissionXp(double profissionXp) {
        this.profissionXp = profissionXp;
    }

    public void setProfissionLevel(int profissionLevel) {
        this.profissionLevel = profissionLevel;
    }
}

package kendo.me.kproffesionscore.professions.database.connection.dao;

import kendo.me.kproffesionscore.professions.Combatente;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CombatenteDao {

    private final Connection connection;

    public CombatenteDao(Connection connection) {
        this.connection = connection;
    }

    public void save(Combatente c) {
        String sql = """
                INSERT INTO combatente (nick, professionLevel, professionXp, mastery, forgelevel, masterySword)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(nick) DO UPDATE SET
                professionLevel=excluded.professionLevel,
                professionXp=excluded.professionXp,
                mastery=excluded.mastery,
                forgelevel=excluded.forgelevel,
                masterySword=excluded.masterySword;
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getNick());
            ps.setInt(2, c.getProfissionLevel());
            ps.setDouble(3, c.getProfissionXp());
            ps.setDouble(4, c.getMastery());
            ps.setInt(5, c.getForgelevel());
            ps.setDouble(6, c.getMasterySword());
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Erro ao salvar Combatente: " + e.getMessage());
        }
    }

    public Combatente load(String nick) {
        String sql = "SELECT * FROM combatente WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Combatente(
                        rs.getString("nick"),
                        rs.getInt("professionLevel"),
                        rs.getDouble("professionXp"),
                        rs.getDouble("mastery"),
                        rs.getInt("forgelevel"),
                        rs.getDouble("masterySword")
                );
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Erro ao carregar Combatente: " + e.getMessage());
        }
        return null;
    }
}
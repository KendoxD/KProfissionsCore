package kendo.me.kproffesionscore.professions.database.connection.dao;

import kendo.me.kproffesionscore.professions.Medico;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MedicoDao {

    private final Connection connection;

    public MedicoDao(Connection connection) {
        this.connection = connection;
    }

    public void save(Medico m) {
        String sql = """
                INSERT INTO medico (nick, professionLevel, professionXp, mastery, healLevel, medicinePower, medicalMastery)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(nick) DO UPDATE SET
                professionLevel=excluded.professionLevel,
                professionXp=excluded.professionXp,
                mastery=excluded.mastery,
                healLevel=excluded.healLevel,
                medicinePower=excluded.medicinePower,
                medicalMastery=excluded.medicalMastery;
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, m.getNick());
            ps.setInt(2, m.getProfissionLevel());
            ps.setDouble(3, m.getProfissionXp());
            ps.setDouble(4, m.getMastery());
            ps.setInt(5, m.getHealLevel());
            ps.setInt(6, m.getMedicinePower());
            ps.setInt(7, m.getMedicalMastery());
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Erro ao salvar Medico: " + e.getMessage());
        }
    }

    public Medico load(String nick) {
        String sql = "SELECT * FROM medico WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Medico(
                        rs.getString("nick"),
                        rs.getInt("professionLevel"),
                        rs.getDouble("professionXp"),
                        rs.getDouble("mastery"),
                        rs.getInt("healLevel"),
                        rs.getInt("medicinePower"),
                        rs.getInt("medicalMastery")
                );
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Erro ao carregar Medico: " + e.getMessage());
        }
        return null;
    }
}
package kendo.me.kproffesionscore.professions.database.connection;

import kendo.me.kproffesionscore.professions.Medico;
import kendo.me.kproffesionscore.professions.database.connection.dao.MedicoDao;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;

public class ProfissionDatabase {

    private final File databaseFile;
    private Connection connection;

    public ProfissionDatabase(File pluginFolder) {
        this.databaseFile = new File(pluginFolder, "professions.db");
        connect();
        createTables();
    }

    public void connect() {
        try {
            if (!databaseFile.exists()) {
                databaseFile.getParentFile().mkdirs();
                databaseFile.createNewFile();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            Bukkit.getLogger().info("[KProfessionsCore] SQLite conectado com sucesso!");
        } catch (Exception e) {
            Bukkit.getLogger().severe("[KProfessionsCore] Erro ao conectar SQLite: " + e.getMessage());
        }
    }

    public void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS combatente (nick TEXT PRIMARY KEY, professionLevel INTEGER, professionXp REAL, mastery REAL, forgeLevel INTEGER, masterySword REAL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS medico (nick TEXT PRIMARY KEY, professionLevel INTEGER, professionXp REAL, mastery REAL, healLevel INTEGER, medicinePower INTEGER, medicalMastery INTEGER);");
            stmt.execute("CREATE TABLE IF NOT EXISTS cozinheiro (nick TEXT PRIMARY KEY, professionLevel INTEGER, professionXp REAL, mastery REAL, healLevel INTEGER, medicinePower INTEGER);");
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[KProfessionsCore] Erro ao criar tabelas SQLite: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Busca o nível do jogador dinamicamente por profissão
     */
    public int getPlayerLevel(String nick, String profession) {
        if (profession == null) return 0;

        if (profession.equalsIgnoreCase("medico")) {
            MedicoDao medicoDao = new MedicoDao(connection);
            Medico m = medicoDao.load(nick);
            return (m != null) ? m.getProfissionLevel() : 0;
        }

        String sql = "SELECT professionLevel FROM " + profession.toLowerCase() + " WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("professionLevel");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean playerExists(String nick) {
        String[] tables = {"combatente", "medico", "cozinheiro"};
        for (String table : tables) {
            String sql = "SELECT 1 FROM " + table + " WHERE nick = ? LIMIT 1";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, nick);
                if (ps.executeQuery().next()) return true;
            } catch (SQLException ignored) {}
        }
        return false;
    }

    public String getPlayerProfession(String nick) {
        String[] tables = {"combatente", "medico", "cozinheiro"};
        for (String table : tables) {
            String sql = "SELECT 1 FROM " + table + " WHERE nick = ? LIMIT 1";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, nick);
                if (ps.executeQuery().next()) return table;
            } catch (SQLException ignored) {}
        }
        return null;
    }
}
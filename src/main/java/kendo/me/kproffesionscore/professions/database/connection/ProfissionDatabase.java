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
    /**
     * Atribui uma profissão a um jogador, inserindo um registo novo
     * com valores iniciais. Não faz nada se o jogador já tiver essa profissão.
     */
    public boolean assignProfession(String nick, String profession) {
        String table = profession.toLowerCase();
        String sql;
        switch (table) {
            case "medico":
                sql = "INSERT OR IGNORE INTO medico (nick, professionLevel, professionXp, mastery, healLevel, medicinePower, medicalMastery) VALUES (?, 1, 0, 0, 1, 0, 0)";
                break;
            case "cozinheiro":
                sql = "INSERT OR IGNORE INTO cozinheiro (nick, professionLevel, professionXp, mastery, healLevel, medicinePower) VALUES (?, 1, 0, 0, 1, 0)";
                break;
            case "combatente":
                sql = "INSERT OR IGNORE INTO combatente (nick, professionLevel, professionXp, mastery, forgeLevel, masterySword) VALUES (?, 1, 0, 0, 1, 0)";
                break;
            default:
                Bukkit.getLogger().warning("[KProfessionsCore] Profissão desconhecida: " + profession);
                return false;
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[KProfessionsCore] Erro ao atribuir profissão: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove a profissão do jogador na tabela indicada.
     */
    public boolean removeProfession(String nick, String profession) {
        String sql = "DELETE FROM " + profession.toLowerCase() + " WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[KProfessionsCore] Erro ao remover profissão: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove a profissão atual do jogador, seja ela qual for (detecta automaticamente a tabela).
     */
    public boolean removeProfession(String nick) {
        String profession = getPlayerProfession(nick);
        if (profession == null) return false;
        return removeProfession(nick, profession);
    }

    /**
     * Adiciona XP à profissão do jogador. Se subir de nível o suficiente
     * (baseado em xpNeeded), aumenta professionLevel e reseta o excesso de XP.
     */

    public boolean addXp(String nick, String profession, double amount) {
        String table = profession.toLowerCase();
        String selectSql = "SELECT professionLevel, professionXp FROM " + table + " WHERE nick = ?";
        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setString(1, nick);
            ResultSet rs = select.executeQuery();
            if (!rs.next()) {
                Bukkit.getLogger().warning("[KProfessionsCore] Jogador " + nick + " não tem registo em " + table);
                return false;
            }

            int level = rs.getInt("professionLevel");
            double xp = rs.getDouble("professionXp") + amount;

            // Curva de XP simples: 100 * nível para subir de nível. Ajusta conforme precisares.
            double xpNeeded = level * 100.0;
            while (xp >= xpNeeded) {
                xp -= xpNeeded;
                level++;
                xpNeeded = level * 100.0;
            }

            String updateSql = "UPDATE " + table + " SET professionLevel = ?, professionXp = ? WHERE nick = ?";
            try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                update.setInt(1, level);
                update.setDouble(2, xp);
                update.setString(3, nick);
                update.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[KProfessionsCore] Erro ao adicionar XP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adiciona XP detectando automaticamente a profissão do jogador.
     */
    public boolean addXp(String nick, double amount) {
        String profession = getPlayerProfession(nick);
        if (profession == null) return false;
        return addXp(nick, profession, amount);
    }

    /**
     * Retorna o XP atual do jogador na profissão indicada.
     */
    public double getPlayerXp(String nick, String profession) {
        String sql = "SELECT professionXp FROM " + profession.toLowerCase() + " WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("professionXp");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Define diretamente o nível de profissão do jogador (sem mexer no XP).
     */
    public boolean setPlayerLevel(String nick, String profession, int level) {
        String sql = "UPDATE " + profession.toLowerCase() + " SET professionLevel = ? WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, level);
            ps.setString(2, nick);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[KProfessionsCore] Erro ao definir nível: " + e.getMessage());
            return false;
        }
    }

}
package kendo.me.kproffesionscore.professions.database.connection;

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

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS combatente (
                        nick TEXT PRIMARY KEY,
                        professionLevel INTEGER,
                        professionXp REAL,
                        mastery REAL,
                        forgeLevel INTEGER,
                        masterySword REAL
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS medico (
                        nick TEXT PRIMARY KEY,
                        professionLevel INTEGER,
                        professionXp REAL,
                        mastery REAL,
                        healLevel INTEGER,
                        medicinePower INTEGER,
                        medicalMastery INTEGER
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cozinheiro (
                        nick TEXT PRIMARY KEY,
                        professionLevel INTEGER,
                        professionXp REAL,
                        mastery REAL,
                        healLevel INTEGER,
                        medicinePower INTEGER
                    );
                    """);

        } catch (SQLException e) {
            Bukkit.getLogger().severe("[KProfessionsCore] Erro ao criar tabelas SQLite: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Bukkit.getLogger().info("[KProfessionsCore] Conexão SQLite encerrada.");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[KProfessionsCore] Erro ao fechar SQLite: " + e.getMessage());
        }
    }


    /**
     * Verifica se o nick do jogador existe em alguma tabela de profissões.
     *
     * @param nick Nick do jogador
     * @return true se existir em alguma tabela, false caso contrário
     */
    public boolean playerExists(String nick) {
        String[] tables = {"combatente", "medico", "cozinheiro"};

        for (String table : tables) {
            String sql = "SELECT 1 FROM " + table + " WHERE nick = ? LIMIT 1";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, nick);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return true; // achou o nick em alguma tabela
                }
            } catch (SQLException e) {
                Bukkit.getLogger().severe("[KProfessionsCore] Erro ao verificar nick em " + table + ": " + e.getMessage());
            }
        }

        return false;
    }

    /**
     * Retorna a profissão do jogador com base no nick.
     *
     * @param nick Nick do jogador
     * @return "combatente", "medico", "cozinheiro" ou null se não tiver profissão
     */
    public String getPlayerProfession(String nick) {
        String[] tables = {"combatente", "medico", "cozinheiro"};

        for (String table : tables) {
            String sql = "SELECT 1 FROM " + table + " WHERE nick = ? LIMIT 1";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, nick);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return table; // retorna o nome da tabela/profissão
                }
            } catch (SQLException e) {
                Bukkit.getLogger().severe("[KProfessionsCore] Erro ao verificar profissão em " + table + ": " + e.getMessage());
            }
        }

        return null; // jogador não possui profissão
    }
}

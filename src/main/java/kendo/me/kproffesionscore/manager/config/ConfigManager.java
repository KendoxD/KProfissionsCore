package kendo.me.kproffesionscore.manager.config;

import kendo.me.kproffesionscore.manager.config.paths.ConfigFiles;
import kendo.me.kproffesionscore.manager.config.paths.ConfigPaths;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class ConfigManager {


    private JavaPlugin plugin;
    private String name;
    private File file;
    private YamlConfiguration config;

    public ConfigManager(JavaPlugin plugin, String name){
        this.plugin = plugin;
        this.name = name;
    }
    public ConfigManager(JavaPlugin plugin){
        this.plugin = plugin;
    }

    /**
     * Inicializa os diretorios com base no ENUM ConfigPaths.
     * @throws IOException
     */
    public void initDirectorys() throws IOException {
        plugin.getLogger().severe(ChatUtils.color("&cChecking current directories.. "));
       for(ConfigPaths path : ConfigPaths.values()){
           File directory = new File(plugin.getDataFolder(), path.getPath());
           if(directory.exists() && !directory.isFile()){
               plugin.getLogger().severe(ChatUtils.color("&aDirectory already exists."));
               plugin.getLogger().severe("Path: " + directory.getPath());
               continue;
           }
           boolean created = directory.mkdirs();
           if(created){
               plugin.getLogger().severe(ChatUtils.color("&aDiretorios criados com sucesso!"));
               plugin.getLogger().severe("Directory: " + directory.getPath());
           }
       }
    }

    public void initFixedFiles() throws IOException {
        createFile(ConfigPaths.CRAFTS.getPath(), "medico.yml");
        createFile(ConfigPaths.CRAFTS.getPath(), "cozinheiro.yml");
        createFile(ConfigPaths.CRAFTS.getPath(), "combatente.yml");
        createFile(ConfigPaths.PROFISSOES.getPath(), "medico.yml");
        createFile(ConfigPaths.PROFISSOES.getPath(), "cozinheiro.yml");
        createFile(ConfigPaths.PROFISSOES.getPath(), "combatente.yml");
    }

    /***
     * Cria os arquivos / diretorios principais ao inicializar o servidor caso nao existam ainda.
     * @param directory diretorio a ser criado
     * @param fileName nome do arquivo
     * @throws IOException
     */
    private void createFile(String directory, String fileName) throws IOException {
        File folder = new File(plugin.getDataFolder(), directory);
        if(!folder.exists()) folder.mkdirs();
        File file = new File(folder, fileName);
        boolean created = false;
        if(!file.exists()) {
            created = file.createNewFile();
        }
        if(created){
            if(directory.equals(ConfigPaths.CRAFTS.getPath())){
                //adiciona a section de crafts ao criar o arquivo pra facilitar alteracoes.
                YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(directory, fileName));
                config.createSection("Craft");
                saveYaml(config, directory, fileName);
                System.out.println("Section crafts created successfully");
            }
            plugin.getLogger().severe(ChatUtils.color("&a File created successfully!"));
            plugin.getLogger().severe("Path: " + file.getPath());
        }

    }
    /**
     * Recarrega um arquivo YAML específico, atualizando os valores na memória.
     * @param fileName Nome do arquivo
     * @param directory diretorio relativo à pasta do plugin
     * @return YamlConfiguration recarregado ou null se não existir
     */
    public @Nullable YamlConfiguration reloadFile(String directory, String fileName) {
        File folder = new File(plugin.getDataFolder(), directory);
        if (!folder.exists()) {
            plugin.getLogger().severe(ChatUtils.color("&cDirectory doesn't exist: " + folder.getPath()));
            return null;
        }

        File targetFile = new File(folder, fileName);
        if (!targetFile.exists()) {
            plugin.getLogger().severe(ChatUtils.color("&cFile doesn't exist: " + targetFile.getPath()));
            return null;
        }

        try {
            config = YamlConfiguration.loadConfiguration(targetFile);
            plugin.getLogger().severe(ChatUtils.color("&aFile reloaded successfully: " + targetFile.getPath()));
            return config;
        } catch (Exception e) {
            plugin.getLogger().severe(ChatUtils.color("&cFailed to reload file: " + targetFile.getPath()));
            e.printStackTrace();
            return null;
        }
    }


    public void reloadAllCraftFiles() {
        String[] craftFiles = {"medico.yml", "cozinheiro.yml", "combatente.yml"};

        for (String fileName : craftFiles) {
            YamlConfiguration file = reloadFile(ConfigPaths.CRAFTS, fileName);
            if (file == null) {
                plugin.getLogger().severe(ChatUtils.color("&cFailed to reload craft file: " + fileName));
                continue;
            }
            plugin.getLogger().severe(ChatUtils.color("&aCraft file reloaded: " + fileName));
        }
    }
    public @Nullable YamlConfiguration reloadFile(ConfigPaths path, String fileName){
        return reloadFile(path.getPath(), fileName);
    }


    public void saveYaml(YamlConfiguration config, String directory, String fileName) {
        File file = new File(plugin.getDataFolder(), directory + "/" + fileName);
        try {
            config.save(file);
            plugin.getLogger().info("File saved: " + file.getPath());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save file: " + file.getPath());
            e.printStackTrace();
        }
    }




    public void createFileIfNotExists() throws IOException {
        File fileToCheck = new File(plugin.getDataFolder().getParent(), this.name);
        if(fileToCheck.exists()){
            this.file = fileToCheck;
            return;
        }
        fileToCheck.createNewFile();
        this.file = fileToCheck;
        config = YamlConfiguration.loadConfiguration(this.file);
    }


    public @Nullable YamlConfiguration loadConfig(ConfigPaths paths){
        this.file = paths.getFile(plugin);
        if(file.exists()) {
            return config = YamlConfiguration.loadConfiguration(file);
        }

        plugin.getLogger().severe(ChatUtils.color("&eFile doesn't exists: " + file.getPath()));
        return null;

    }


    public @Nullable YamlConfiguration getCraftFile(ConfigPaths path, ConfigFiles configFiles){
        file = path.getFile(plugin);
        if(file.exists()){
            return config = YamlConfiguration.loadConfiguration(new File(file.getPath() + "/"+configFiles.getFileName()));
        }
        plugin.getLogger().severe(ChatUtils.color("&eFile doesn't exists: " + file.getPath()));
        return null;
    }


    /**
     * Metodo que verifica se o craft ja existe na config
     * @param craftName - nome do craft na config
     * @return boolean - true existe na config, false nao existe
     */
    public boolean checkIfCraftExists(String craftName){
        String basePath = "Craft." + craftName;
        return config.contains(basePath);
    }

    public boolean checkIfIngredientExists(String craftName){
        String basePath = "Craft." + craftName;
        if(config.contains(basePath + ".ingredients")){
            return true;
        }
        return false;
    }

    public boolean checkIfSlotIsOcuppied(String craftName, int slot){
        String basePath = "Craft." + craftName +".ingredients"+"."+slot;
        return config.contains(basePath);
    }

    /**
     *
     * @param craftName
     * @return
     */
    public int getIngredientSize(String craftName){
        String basePath = "Craft." + craftName+".ingredients";
        int current = config.contains(basePath)
                ? config.getConfigurationSection(basePath).getKeys(false).size()
                : 0;

        return current;
    }


}

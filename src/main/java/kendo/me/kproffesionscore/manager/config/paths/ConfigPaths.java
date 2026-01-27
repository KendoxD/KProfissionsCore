package kendo.me.kproffesionscore.manager.config.paths;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public enum ConfigPaths {

    PROFISSOES("profissoes"),
    CRAFTS(PROFISSOES, "crafts");

    private final String path;

    ConfigPaths(String path) {
        this.path = path;
    }

    ConfigPaths(ConfigPaths parent, String child) {
        this.path = parent.getPath() + File.separator + child;
    }

    public String getPath() {
        return path;
    }
    public File getFile(JavaPlugin plugin) {
        return new File(plugin.getDataFolder(), path);
    }
}

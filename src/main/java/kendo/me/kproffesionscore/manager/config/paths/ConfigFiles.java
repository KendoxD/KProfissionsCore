package kendo.me.kproffesionscore.manager.config.paths;

public enum ConfigFiles {

    MEDICO("medico.yml"),
    COZINHEIRO("cozinheiro.yml"),
    COMBATENTE("combatente.yml");

    private final String fileName;

    ConfigFiles(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return fileName;
    }
}

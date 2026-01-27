package kendo.me.kproffesionscore.commands.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface CommandAction {
    void execute(Player player, @Nullable String[] args);
}

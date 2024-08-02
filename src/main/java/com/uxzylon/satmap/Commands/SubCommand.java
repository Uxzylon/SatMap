package com.uxzylon.satmap.Commands;

import org.bukkit.entity.Player;

import java.util.*;

public abstract class SubCommand {
    public abstract String getName();
    public abstract String getDescription();
    public abstract String getSyntax();
    public abstract String permission();
    public abstract boolean canRunConsole();
    public int getMinArgs() {
        return 0;
    }
    public abstract List<String> getSubcommandArguments(Player player, String[] args);
    public abstract void perform(Player player, String[] args);
}

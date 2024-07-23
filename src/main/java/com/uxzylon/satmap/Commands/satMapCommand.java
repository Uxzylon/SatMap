package com.uxzylon.satmap.Commands;

import com.uxzylon.satmap.Commands.subcommands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.uxzylon.satmap.SatMap.plugin;

public class satMapCommand implements TabExecutor {

    private final ArrayList<SubCommand> subCommands = new ArrayList<>();
    public satMapCommand() {
        subCommands.add(new test());
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }

    public void help(Player player) {
        player.sendMessage("Satmap");
        for (int i=0; i < getSubCommands().size(); i++) {
            if (player.hasPermission(getSubCommands().get(i).permission())) {
                player.sendMessage(getSubCommands().get(i).getSyntax() + " - " + ChatColor.GRAY + getSubCommands().get(i).getDescription());
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length > 0) {
                boolean found = false;
                for (int i=0; i < getSubCommands().size(); i++) {
                    if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                        found = true;
                        if (player.hasPermission(getSubCommands().get(i).permission())) {
                            getSubCommands().get(i).perform(player, args);
                        } else {
                            player.sendMessage("You don't have permission to run this command.");
                        }
                    }
                }
                if (!found) {
                    help(player);
                }
            } else {
                help(player);
            }
        } else {
            if (args.length > 0) {
                for (int i=0; i < getSubCommands().size(); i++) {
                    if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                        if (getSubCommands().get(i).canRunConsole()) {
                            getSubCommands().get(i).perform(null, args);
                        } else {
                            plugin.getLogger().info("This command can't be run from console.");
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        Player player = (Player) sender;
        if (args.length == 1) {
            ArrayList<String> subCommandsArguments = new ArrayList<>();
            for (int i=0; i < getSubCommands().size(); i++) {
                if (player.hasPermission(getSubCommands().get(i).permission())) {
                    subCommandsArguments.add(getSubCommands().get(i).getName());
                }
            }
            return subCommandsArguments;
        } else if (args.length >= 2) {
            for (int i=0; i < getSubCommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                    return getSubCommands().get(i).getSubcommandArguments((Player) sender, args);
                }
            }
        }
        return null;
    }
}

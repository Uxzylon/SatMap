package com.uxzylon.satmap.Commands;

import com.uxzylon.satmap.Commands.subcommands.*;
import com.uxzylon.satmap.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.uxzylon.satmap.SatMap.*;
import static com.uxzylon.satmap.Text.sendMessage;

public class satMapCommand implements TabExecutor {

    private final ArrayList<SubCommand> subCommands = new ArrayList<>();
    public satMapCommand() {
        subCommands.add(new Place());
        subCommands.add(new Load());
        subCommands.add(new Reload());
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }

    public void help(Player player) {
        sendMessage(player, Text.title, pluginName);
        for (int i=0; i < getSubCommands().size(); i++) {
            if (player.hasPermission(getSubCommands().get(i).permission())) {
                sendMessage(player, getSubCommands().get(i).getSyntax() + " - " + ChatColor.GRAY + getSubCommands().get(i).getDescription());
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                help(player);
            }
            return true;
        }

        for (SubCommand subCommand : getSubCommands()) {
            if (args[0].equalsIgnoreCase(subCommand.getName())) {
                if (sender instanceof Player player) {
                    handlePlayerCommand(player, subCommand, args);
                } else {
                    handleConsoleCommand(subCommand, args);
                }
                return true;
            }
        }

        if (sender instanceof Player player) {
            help(player);
        }
        return true;
    }

    private void handlePlayerCommand(Player player, SubCommand subCommand, String[] args) {
        if (player.hasPermission(subCommand.permission())) {
            if (subCommand.getMinArgs() <= args.length) {
                subCommand.perform(player, args);
            } else {
                sendMessage(player, subCommand.getSyntax());
            }
        } else {
            sendMessage(player, Text.noPermission);
        }
    }

    private void handleConsoleCommand(SubCommand subCommand, String[] args) {
        if (subCommand.canRunConsole()) {
            subCommand.perform(null, args);
        } else {
            sendMessage(console, Text.playerOnly);
        }
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

package com.uxzylon.satmap.Commands.subcommands;

import com.uxzylon.satmap.Commands.SubCommand;
import com.uxzylon.satmap.Text;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static com.uxzylon.satmap.SatMap.*;
import static com.uxzylon.satmap.Text.sendMessage;

public class Reload extends SubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return Text.reloadDescription.getText();
    }

    @Override
    public String getSyntax() {
        return "/satmap reload";
    }

    @Override
    public String permission() {
        return "satmap.command.reload";
    }

    @Override
    public boolean canRunConsole() {
        return true;
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void perform(Player player, String[] args) {
        plugin.reloadConfig();
        rgbBlockColor.updateForbiddenMaterials();

        CommandSender[] senders = {console};
        if (player != null) {
            senders = ArrayUtils.add(senders, player);
        }

        sendMessage(senders, Text.pluginReloaded);
    }
}
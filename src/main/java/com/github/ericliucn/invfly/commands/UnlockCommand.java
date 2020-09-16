package com.github.ericliucn.invfly.commands;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.datas.EnderChestSyncData;
import com.github.ericliucn.invfly.data.datas.PlayerInvSyncData;
import com.github.ericliucn.invfly.utils.Utils;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class UnlockCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Message message = Invfly.instance.getConfigLoader().getMessage();
        args.<Player>getOne("player").ifPresent(player -> {
            EnderChestSyncData.FREEZE.remove(player.getUniqueId());
            PlayerInvSyncData.FREEZE.remove(player.getUniqueId());
            src.sendMessage(message.getMessage("command.unlock").replace("%player%", Utils.toText(player.getName())));
        });
        return null;
    }

    public static CommandSpec build(){
        return CommandSpec
                .builder()
                .permission("invfly.command.lock")
                .arguments(GenericArguments.player(Text.of("player")))
                .executor(new UnlockCommand())
                .build();
    }
}

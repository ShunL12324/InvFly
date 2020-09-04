package com.github.ericliucn.invfly.commands;


import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.SyncDataService;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.utils.Utils;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

public class LoadCommand implements CommandExecutor {


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Message message = Invfly.instance.getConfigLoader().getMessage();
        args.<User>getOne("user").ifPresent(user -> {
            SyncDataService syncDataService = Invfly.instance.getService();
            try {
                syncDataService.loadUerData(user);
                src.sendMessage(Utils.toText(message.loadSuccessful));
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                src.sendMessage(Utils.toText(message.loadFail));
            }
        });
        return CommandResult.success();
    }

    public static CommandSpec build(){
        return CommandSpec.builder()
                .executor(new LoadCommand())
                .permission("invfly.command.load")
                .arguments(GenericArguments.user(Text.of("user")))
                .build();
    }
}

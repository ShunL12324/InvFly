package com.github.ericliucn.invfly.commands;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.utils.Utils;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;

public class ReloadCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Message message = Invfly.instance.getConfigLoader().getMessage();
        try {
            Invfly.instance.reload();
            src.sendMessage(Utils.toText(message.reloadSuccess));
        }catch (Exception e){
            e.printStackTrace();
            src.sendMessage(Utils.toText(message.reloadFail));
        }
        return CommandResult.success();
    }

    public static CommandSpec build(){
        return CommandSpec.builder()
                .permission("invfly.command.reload")
                .executor(new ReloadCommand())
                .build();
    }
}

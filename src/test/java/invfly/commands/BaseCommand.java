package invfly.commands;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;

public class BaseCommand implements CommandExecutor {


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        return CommandResult.success();
    }

    public static CommandSpec spec(){
        return CommandSpec.builder()
                .executor(new BaseCommand())
                .child(LoadCommand.build(), "load")
                .child(SaveCommand.build(), "save")
                .child(ReloadCommand.build(), "reload")
                .child(ListCommand.build(), "list")
                .build();
    }
}

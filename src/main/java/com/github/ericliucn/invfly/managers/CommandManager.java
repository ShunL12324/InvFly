package com.github.ericliucn.invfly.managers;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.commands.BaseCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.spec.CommandSpec;


public class CommandManager {

    public CommandManager(){
        this.register(BaseCommand.spec(), "invfly");
    }

    private void register(CommandSpec commandSpec, String... alias){
        Sponge.getCommandManager().register(Invfly.instance, commandSpec, alias);
    }

    private void unregister(CommandMapping mapping){
        Sponge.getCommandManager().removeMapping(mapping);
    }

    public void unload(){
        Sponge.getCommandManager().getOwnedBy(Invfly.instance).forEach(this::unregister);
    }
}

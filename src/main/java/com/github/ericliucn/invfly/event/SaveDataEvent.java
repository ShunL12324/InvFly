package com.github.ericliucn.invfly.event;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.data.StorageData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.plugin.PluginContainer;

public class SaveDataEvent implements SyncDataEvent{

    private final User user;
    private final StorageData storageData;
    private final PluginContainer container;

    public SaveDataEvent(User user, StorageData storageData){
        this.container = Invfly.instance.getPluginContainer();
        this.user = user;
        this.storageData = storageData;
        Sponge.getEventManager().post(this);
    }

    @Override
    public Cause getCause() {
        EventContext context = EventContext.builder().add(EventContextKeys.PLUGIN, container).build();
        return Cause.builder().append(Invfly.instance).build(context);
    }

    @Override
    public Object getSource() {
        return user;
    }

    @Override
    public EventContext getContext() {
        return EventContext.builder().add(EventContextKeys.PLUGIN, container).build();
    }

    @Override
    public StorageData getStorageData() {
        return storageData;
    }

    @Override
    public User getTargetUser() {
        return user;
    }

}

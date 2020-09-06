package com.github.ericliucn.invfly.event;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.data.SyncData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.user.TargetUserEvent;

import java.util.UUID;

public class PreLoadDataEvent implements TargetUserEvent,SyncDataEvent {

    private final UUID uuid;
    private final StorageData data;
    private final User user;
    private final SyncData syncData;

    public PreLoadDataEvent(User user, StorageData data, SyncData syncData){
        this.uuid = user.getUniqueId();
        this.data = data;
        this.syncData = syncData;
        this.user = user;
        Sponge.getEventManager().post(this);
    }

    @Override
    public User getTargetUser() {
        return user;
    }

    @Override
    public Cause getCause() {
        EventContext context = EventContext.builder().add(EventContextKeys.PLUGIN, Invfly.instance.getPluginContainer()).build();
        return Cause.builder().append(Invfly.instance).build(context);
    }

    public StorageData getData(){
        return data;
    }

    public UUID getUuid(){
        return uuid;
    }

    @Override
    public StorageData getStorageData() {
        return data;
    }

    @Override
    public SyncData getSyncData() {
        return syncData;
    }
}

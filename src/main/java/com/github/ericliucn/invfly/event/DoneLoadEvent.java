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

public class DoneLoadEvent implements TargetUserEvent, SyncDataEvent {

    private final UUID uuid;
    private final StorageData data;
    private final User user;
    private final EnumResult result;
    private final SyncData syncData;

    public DoneLoadEvent(User user, StorageData data, EnumResult enumResult, SyncData syncData){
        this.uuid = user.getUniqueId();
        this.data = data;
        this.result = enumResult;
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

    public UUID getUuid(){
        return uuid;
    }

    public EnumResult getResult() {
        return result;
    }

    @Override
    public StorageData getStorageData() {
        return this.data;
    }

    public SyncData getSyncData() {
        return syncData;
    }

}

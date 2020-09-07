package com.github.ericliucn.invfly.event;

import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.data.SyncData;
import org.spongepowered.api.event.user.TargetUserEvent;

import java.util.UUID;

public interface SyncSingleEvent extends TargetUserEvent {

    StorageData getStorageData();

    SyncData getSyncData();

    default String getID(){
        return getSyncData().getID();
    }

    UUID getParentTaskUUID();
}

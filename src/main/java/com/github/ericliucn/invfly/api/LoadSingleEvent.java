package com.github.ericliucn.invfly.api;

import com.github.ericliucn.invfly.data.StorageData;
import org.spongepowered.api.event.user.TargetUserEvent;

import java.util.UUID;

public interface LoadSingleEvent extends TargetUserEvent {

    StorageData getStorageData();

    SyncData getSyncData();

    default String getID(){
        return getSyncData().getID();
    }

    UUID getParentTaskUUID();
}

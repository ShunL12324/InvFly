package com.github.ericliucn.invfly.event;

import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.data.SyncData;
import org.spongepowered.api.event.user.TargetUserEvent;

public interface SyncDataEvent extends TargetUserEvent {

    StorageData getStorageData();

    SyncData getSyncData();

    default String getID(){
        return getSyncData().getID();
    }
}

package com.github.ericliucn.invfly.event;

import com.github.ericliucn.invfly.data.StorageData;
import org.spongepowered.api.event.user.TargetUserEvent;

public interface SyncDataEvent extends TargetUserEvent {

    StorageData getStorageData();
}

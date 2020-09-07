package com.github.ericliucn.invfly.event;

import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.data.SyncData;
import org.spongepowered.api.event.user.TargetUserEvent;

import java.util.List;
import java.util.UUID;

public interface SyncAllEvent extends TargetUserEvent {

    StorageData getStorageData();

    List<SyncData> getSyncDataList();

    UUID getTaskUUID();
}

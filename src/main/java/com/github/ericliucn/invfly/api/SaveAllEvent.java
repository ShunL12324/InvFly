package com.github.ericliucn.invfly.api;

import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.StorageData;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.user.TargetUserEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SaveAllEvent extends TargetUserEvent {

    UUID getTaskUUID();

    StorageData getStorageData();

    List<SyncData> getSyncDataList();

    interface Pre extends SaveAllEvent{

    }

    interface Done extends SaveAllEvent{

        Map<SyncData, EnumResult> getResults();
    }

}

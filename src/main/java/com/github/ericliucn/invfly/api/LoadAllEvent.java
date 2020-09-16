package com.github.ericliucn.invfly.api;

import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.StorageData;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.user.TargetUserEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LoadAllEvent extends TargetUserEvent {

    /**
     * @return The storageData will be use in sync
     */
    StorageData getStorageData();

    /**
     * @return Which data type will be sync
     */
    List<SyncData> getSyncDataList();

    /**
     * @return The unique task ID
     */
    UUID getTaskUUID();

    CommandSource getCmdSource();

    /**
     * Before the actual sync operation start
     */
    interface Pre extends LoadAllEvent {

    }


    /**
     * After the sync operation done
     */
    interface Done extends LoadAllEvent {

        /**
         * @return The result of the finished sync operation
         */
        Map<SyncData, EnumResult> getResultMap();
    }
}

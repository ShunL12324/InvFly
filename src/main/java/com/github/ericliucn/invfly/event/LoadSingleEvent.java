package com.github.ericliucn.invfly.event;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.api.SyncData;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.StorageData;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;

import java.util.UUID;

public class LoadSingleEvent {

    public static class Pre implements com.github.ericliucn.invfly.api.LoadSingleEvent {

        private final UUID parentTaskUUID;
        private final StorageData data;
        private final SyncData syncData;
        private final User user;

        public Pre(UUID parentTaskUUID, User user, StorageData data, SyncData syncData){
            this.parentTaskUUID = parentTaskUUID;
            this.data = data;
            this.syncData = syncData;
            this.user = user;
        }

        @Override
        public StorageData getStorageData() {
            return this.data;
        }

        @Override
        public SyncData getSyncData() {
            return this.syncData;
        }

        @Override
        public UUID getParentTaskUUID() {
            return this.parentTaskUUID;
        }

        @Override
        public User getTargetUser() {
            return this.user;
        }

        @Override
        public Cause getCause() {
            EventContext context = EventContext.builder().add(EventContextKeys.PLUGIN, Invfly.instance.getPluginContainer()).build();
            return Cause.builder().append(Invfly.instance).build(context);
        }
    }

    public static class Done implements com.github.ericliucn.invfly.api.LoadSingleEvent {

        private final UUID parentTaskUUID;
        private final StorageData data;
        private final SyncData syncData;
        private final User user;
        private final EnumResult result;

        public Done(UUID parentTaskUUID, User user, StorageData data, SyncData syncData, EnumResult result){
            this.parentTaskUUID = parentTaskUUID;
            this.data = data;
            this.syncData = syncData;
            this.user = user;
            this.result = result;
        }

        @Override
        public StorageData getStorageData() {
            return this.data;
        }

        public EnumResult getResult() {
            return result;
        }

        @Override
        public SyncData getSyncData() {
            return this.syncData;
        }

        @Override
        public UUID getParentTaskUUID() {
            return this.parentTaskUUID;
        }

        @Override
        public User getTargetUser() {
            return this.user;
        }

        @Override
        public Cause getCause() {
            EventContext context = EventContext.builder().add(EventContextKeys.PLUGIN, Invfly.instance.getPluginContainer()).build();
            return Cause.builder().append(Invfly.instance).build(context);
        }
    }
}

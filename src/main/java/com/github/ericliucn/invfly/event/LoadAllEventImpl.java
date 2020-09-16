package com.github.ericliucn.invfly.event;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.api.LoadAllEvent;
import com.github.ericliucn.invfly.api.SyncData;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.StorageData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoadAllEventImpl implements LoadAllEvent {

    private final UUID taskUUID;
    private final StorageData data;
    private final List<SyncData> dataList;
    private final User user;
    private final CommandSource source;

    public LoadAllEventImpl(UUID taskUUID, User user, List<SyncData> dataList, StorageData data, CommandSource source){
        this.taskUUID = taskUUID;
        this.data = data;
        this.dataList = dataList;
        this.user = user;
        this.source = source;
    }

    @Override
    public StorageData getStorageData() {
        return this.data;
    }

    @Override
    public List<SyncData> getSyncDataList() {
        return this.dataList;
    }

    @Override
    public UUID getTaskUUID() {
        return this.taskUUID;
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

    @Override
    public CommandSource getCmdSource() {
        return this.source;
    }

    public static class Pre extends LoadAllEventImpl implements LoadAllEvent.Pre{


        public Pre(UUID taskUUID, User user, List<SyncData> dataList, StorageData data, CommandSource source){
            super(taskUUID, user, dataList, data, source);
            Sponge.getEventManager().post(this);
        }

    }

    public static class Done extends LoadAllEventImpl implements LoadAllEvent.Done{

        private final Map<SyncData, EnumResult> resultMap;

        public Done(UUID taskUUID, User user, List<SyncData> dataList, StorageData data, Map<SyncData, EnumResult> resultMap, CommandSource source){
            super(taskUUID, user, dataList, data, source);
            this.resultMap = resultMap;
            Sponge.getEventManager().post(this);
        }

        @Override
        public Map<SyncData, EnumResult> getResultMap() {
            return this.resultMap;
        }
    }

}

package com.github.ericliucn.invfly.service;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.api.SaveAllEvent;
import com.github.ericliucn.invfly.api.SyncData;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.event.LoadAllEventImpl;
import com.github.ericliucn.invfly.exception.NoResultException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.util.*;
import java.util.concurrent.Future;

public class SyncDataService {

    private final List<SyncData> syncDataList = new ArrayList<>();
    private final SpongeExecutorService asyncExecutor;
    private final SpongeExecutorService syncExecutor;
    private static final Map<UUID, LoadResult> LOADTASKS = new HashMap<>();
    private static final Map<UUID, SaveResult> SAVETASKS = new HashMap<>();
    private final Message message;

    public SyncDataService(){
        Scheduler scheduler = Sponge.getScheduler();
        asyncExecutor = scheduler.createAsyncExecutor(Invfly.instance);
        syncExecutor = scheduler.createSyncExecutor(Invfly.instance);
        message = Invfly.instance.getConfigLoader().getMessage();
        Sponge.getEventManager().registerListeners(Invfly.instance, this);
    }

    public void loadUerData(User user, boolean checkPermission, CommandSource source) {
        StorageData storageData = Invfly.instance.getDatabaseManager().getLatest(user);
        loadUserData(user, storageData, checkPermission, source);
    }

    public void loadUserData(User user, StorageData data, boolean checkPermission, CommandSource source) {
        UUID uuid = UUID.randomUUID();
        LOADTASKS.put(uuid,  new LoadResult(uuid,syncDataList, user, data, asyncExecutor, syncExecutor, checkPermission, source));
    }

    @Listener
    public void onLoadAllPre(LoadAllEventImpl.Pre event){
        if (event.getCmdSource() == null) return;
        event.getCmdSource().sendMessage(message.getMessage("task.load.loading"));
    }

    @Listener
    public void onLoadAllDone(LoadAllEventImpl.Done event) throws NoResultException {
        if (event.getCmdSource() == null) return;
        UUID taskUUID = event.getTaskUUID();
        LoadResult loadResult = LOADTASKS.get(taskUUID);
        if (loadResult == null) throw new NoResultException(taskUUID);
        if (loadResult.isAllSuccess()){
            event.getCmdSource().sendMessage(message.getMessage("task.load.success"));
        }else {
            event.getCmdSource().sendMessage(message.getMessage("task.load.fail"));
        }
    }

    public void saveUserData(User user, boolean isDisconnect, CommandSource source){
        UUID taskUUID = UUID.randomUUID();
        SAVETASKS.put(taskUUID, new SaveResult(taskUUID, user, syncDataList, this.asyncExecutor, this.syncExecutor, isDisconnect, source));
    }

    @Listener
    public void saveAllEventPre(SaveAllEvent.Pre event){
        if (event.getCmdSource() == null) return;
        event.getCmdSource().sendMessage(message.getMessage("task.save.saving"));
    }

    @Listener
    public void saveAllEventDone(SaveAllEvent.Done event){
        if (event.getCmdSource() == null) return;
        UUID taskUUID = event.getTaskUUID();
        SaveResult saveResult = SAVETASKS.get(taskUUID);
        if (saveResult.isAllSuccess()){
            event.getCmdSource().sendMessage(message.getMessage("task.save.success"));
        }else {
            event.getCmdSource().sendMessage(message.getMessage("task.save.fail"));
        }
    }

    public LoadResult getLoadResult(UUID taskUUID){
        return LOADTASKS.get(taskUUID);
    }

    public Future<Boolean> getExists(UUID uuid){
        return asyncExecutor.submit(() -> Invfly.instance.getDatabaseManager().isDataExists(uuid));
    }

    public void unregisterAll(){
        this.syncDataList.forEach(syncData -> {
            if (syncData.shouldRegListener()){
                syncData.unregisterListener();
            }
        });
        this.syncDataList.clear();
    }

    public void register(SyncData data){
        if (syncDataList.stream().map(SyncData::getID).anyMatch(s -> s.equalsIgnoreCase(data.getID()))){
            Invfly.instance.getLogger().warn("Duplicate Sync Data ID!!!");
        }else {
            this.syncDataList.add(data);
            if (data.shouldRegListener()) Sponge.getEventManager().registerListeners(Invfly.instance, data);
        }
    }

    public void unregister(SyncData data){
        this.syncDataList.stream().filter(syncData -> syncData.getID().equals(data.getID())).forEach(syncData -> {
            if (syncData.shouldRegListener()){
                syncData.unregisterListener();
            }
            syncDataList.remove(syncData);
        });
    }


}

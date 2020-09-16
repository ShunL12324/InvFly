package com.github.ericliucn.invfly.service;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.api.SyncData;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.event.SaveAllEventImpl;
import com.github.ericliucn.invfly.exception.SerializeException;
import com.github.ericliucn.invfly.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SaveResult {

    private final SpongeExecutorService async;
    private final SpongeExecutorService sync;
    private final UUID taskUUID;
    private final User user;
    private final List<SyncData> dataList;
    private final boolean isDisconnect;
    private StorageData storageData;
    private final Map<String, String> stringData = new ConcurrentHashMap<>();
    private final Map<SyncData, EnumResult> serializeResults = new ConcurrentHashMap<>();
    private final AtomicInteger count;
    private final int timeOut;
    private final int listSize;
    private boolean alreadyPost;
    private final static Type TYPE = new TypeToken<Map<String, String>>(){}.getType();
    private final static Gson GSON = new Gson();
    private final CommandSource source;


    public SaveResult(UUID taskUUID, User user, List<SyncData> syncDataList, SpongeExecutorService async, SpongeExecutorService sync, boolean isDisconnect, CommandSource source){
        this.async = async;
        this.sync = sync;
        this.taskUUID = taskUUID;
        this.user = user;
        this.dataList = syncDataList;
        this.isDisconnect = isDisconnect;
        this.timeOut = Invfly.instance.getConfigLoader().getConfig().general.loadTimeOut;
        this.listSize = syncDataList.size();
        this.count = new AtomicInteger(0);
        this.source = source;
        this.serializeData();
        this.timeOutTask();
    }

    private void timeOutTask(){
        sync.schedule(()->{
            if (!alreadyPost){
                postData();
                dataList.stream()
                        .filter(syncData -> !serializeResults.containsKey(syncData))
                        .map(syncData -> serializeResults.put(syncData, EnumResult.FAIL))
                        .close();
            }
        }, timeOut, TimeUnit.SECONDS);
    }

    private void serializeData(){
        Utils.postEvent(new SaveAllEventImpl.Pre(user, taskUUID, dataList, storageData, source));
        for (SyncData syncData:dataList){
            if (syncData.shouldAsync()){
                serializeAsync(syncData);
            }else {
                serializeSync(syncData);
            }
        }
    }

    private void serializeSync(SyncData syncData){
        sync.submit(() -> {
            try {
                String string = syncData.getSerializedData(user);
                this.serializeResults.put(syncData, EnumResult.SUCCESS);
                this.stringData.put(syncData.getID(), string);
            } catch (SerializeException e) {
                e.printStackTrace();
                this.serializeResults.put(syncData, EnumResult.FAIL);
            }
            this.count.getAndIncrement();
            this.checkFinish();
        });
    }

    private void serializeAsync(SyncData syncData){
        async.submit(() -> {
            try {
                String string = syncData.getSerializedData(user);
                this.serializeResults.put(syncData, EnumResult.SUCCESS);
                this.stringData.put(syncData.getID(), string);
            }catch (SerializeException e){
                sync.submit((Runnable) e::printStackTrace);
                this.serializeResults.put(syncData, EnumResult.FAIL);
            }
            this.count.getAndIncrement();
            this.checkFinish();
        });
    }

    private void checkFinish(){
        if (this.count.get() >= this.listSize && !alreadyPost){
            this.postData();
        }
    }

    private void postData(){
        this.storageData = new StorageData(user, GSON.toJson(stringData, TYPE), this.isDisconnect);
        async.submit(() -> Invfly.instance.getDatabaseManager().saveData(this.storageData, user, taskUUID, dataList, serializeResults, source));
        this.alreadyPost = true;
    }

    public UUID getTaskUUID() {
        return taskUUID;
    }

    public User getUser() {
        return user;
    }

    public boolean isAllSuccess(){
        if (serializeResults.size() >= this.listSize){
            long failCount = serializeResults.values().stream().filter(result -> result.equals(EnumResult.FAIL)).count();
            return failCount == 0;
        }
        return false;
    }
}

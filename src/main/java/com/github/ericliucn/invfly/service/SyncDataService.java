package com.github.ericliucn.invfly.service;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.GsonTypes;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.data.SyncData;
import com.github.ericliucn.invfly.event.DoneLoadEvent;
import com.github.ericliucn.invfly.event.PreLoadDataEvent;
import com.github.ericliucn.invfly.exception.DeserializeException;
import com.github.ericliucn.invfly.exception.SerializeException;
import com.github.ericliucn.invfly.managers.DatabaseManager;
import com.google.gson.Gson;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncDataService {

    private static final Gson gson = new Gson();
    private final List<SyncData> syncDataList = new ArrayList<>();
    private final SpongeExecutorService asyncExecutor;
    private final SpongeExecutorService syncExecutor;

    public SyncDataService(){
        asyncExecutor = Sponge.getScheduler().createAsyncExecutor(Invfly.instance);
        syncExecutor = Sponge.getScheduler().createSyncExecutor(Invfly.instance);
    }

    public void loadUerData(User user, boolean checkPermission) {
        StorageData storageData = getDatabaseManager().getLatest(user);
        loadUserData(user, storageData, checkPermission);
    }

    public void saveUserData(User user, boolean isDisconnect){
        Map<String, String> all = new HashMap<>();
        for(SyncData syncData:syncDataList){
            try {
                all.put(syncData.getID(), syncData.getSerializedData(user));
            } catch (SerializeException e) {
                e.printStackTrace();
            }
        }
        StorageData storageData = new StorageData(user, gson.toJson(all, GsonTypes.ALLDATATYPE), isDisconnect);
        getDatabaseManager().saveData(storageData);
    }


    public void loadUserData(User user, StorageData data, boolean checkPermission) {
        for(SyncData syncData:syncDataList){
            loadUserSingleData(user, data, syncData, checkPermission);
        }
    }

    public void loadUserSingleData(User user, StorageData data, SyncData syncData, boolean checkPermission) {
        new PreLoadDataEvent(user, data, syncData);
        String permission = syncData.getPermissionNode();
        if (data == null){
            //storage data null check
            new DoneLoadEvent(user, data, EnumResult.FAIL, syncData);
            return;
        }
        if (checkPermission && !user.hasPermission(permission)){
            // deal no permission
            new DoneLoadEvent(user, data, EnumResult.NOPERMISSOON, syncData);
            return;
        }
        if (data.getSingleData(syncData) == null){
            //deal no data
            new DoneLoadEvent(user,data,EnumResult.NODATA, syncData);
            return;
        }
        String strData = data.getSingleData(syncData);
        if (syncData.shouldAsync()){
            this.asyncExecutor.submit(()->{
                try {
                    syncData.deserialize(user, strData);
                    new DoneLoadEvent(user, data, EnumResult.SUCCESS, syncData);
                } catch (DeserializeException e) {
                    this.syncExecutor.submit((Runnable) e::printStackTrace);
                    new DoneLoadEvent(user,data,EnumResult.FAIL, syncData);
                    //deal deserialize fail
                }
            });
        }else {
            try {
                syncData.deserialize(user, strData);
                new DoneLoadEvent(user, data, EnumResult.SUCCESS, syncData);
            } catch (DeserializeException e) {
                e.printStackTrace();
                //deal deserialize fail
                new DoneLoadEvent(user,data,EnumResult.FAIL, syncData);
            }
        }
    }

    public void register(SyncData data){
        for (SyncData syncData : this.syncDataList) {
            if (syncData.getID().equalsIgnoreCase(data.getID())) {
                return;
            }
        }
        this.syncDataList.add(data);
        if (data.shouldRegListener()){
            Sponge.getEventManager().registerListeners(Invfly.instance, data);
        }
    }


    public StorageData getLatestData(User user){
        return getDatabaseManager().getLatest(user);
    }

    public void unregister(SyncData data){
        this.syncDataList.remove(data);
    }

    public void unregisterAll(){
        this.syncDataList.clear();
    }

    private DatabaseManager getDatabaseManager(){
        return Invfly.instance.getDatabaseManager();
    }



}

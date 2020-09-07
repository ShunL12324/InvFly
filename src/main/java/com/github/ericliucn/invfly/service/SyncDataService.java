package com.github.ericliucn.invfly.service;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.data.GsonTypes;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.data.SyncData;
import com.github.ericliucn.invfly.event.LoadAllEvent;
import com.github.ericliucn.invfly.exception.NoResultException;
import com.github.ericliucn.invfly.exception.SerializeException;
import com.github.ericliucn.invfly.utils.Utils;
import com.google.gson.Gson;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.util.*;

public class SyncDataService {

    private static final Gson gson = new Gson();
    private final List<SyncData> syncDataList = new ArrayList<>();
    private final SpongeExecutorService asyncExecutor;
    private final SpongeExecutorService syncExecutor;
    private static final Map<UUID, LoadResult> LOADTASKS = new HashMap<>();

    public SyncDataService(){
        Scheduler scheduler = Sponge.getScheduler();
        asyncExecutor = scheduler.createAsyncExecutor(Invfly.instance);
        syncExecutor = scheduler.createSyncExecutor(Invfly.instance);
        Sponge.getEventManager().registerListeners(Invfly.instance, this);
    }

    public void loadUerData(User user, boolean checkPermission) {
        StorageData storageData = Invfly.instance.getDatabaseManager().getLatest(user);
        loadUserData(user, storageData, checkPermission);
    }

    public void loadUserData(User user, StorageData data, boolean checkPermission) {
        UUID uuid = UUID.randomUUID();
        LOADTASKS.put(uuid,  new LoadResult(uuid,syncDataList, user, data, asyncExecutor, syncExecutor, checkPermission));
    }

    @Listener
    public void onLoadAllPre(LoadAllEvent.Pre event){
        event.getTargetUser().getPlayer().ifPresent(player -> player.sendMessage(Utils.toText("&b[InvFly] &eLoading")));
    }

    @Listener
    public void onLoadAllDone(LoadAllEvent.Done event) throws NoResultException {
        UUID taskUUID = event.getTaskUUID();
        LoadResult loadResult = LOADTASKS.get(taskUUID);
        if (loadResult == null) throw new NoResultException(taskUUID);
        if (loadResult.isAllSuccess()){
            loadResult.getUser().getPlayer().ifPresent(player -> player.sendMessage(Utils.toText("&b[InvFly] &aLoad Successful!")));
        }else {
            loadResult.getUser().getPlayer().ifPresent(player -> player.sendMessage(Utils.toText("&b[InvFly] &4Fail to load some data!")));
        }
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
        Invfly.instance.getDatabaseManager().saveData(storageData);
    }

    public LoadResult getResult(UUID taskUUID){
        return LOADTASKS.get(taskUUID);
    }

    public void unregisterAll(){
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
        this.syncDataList.remove(data);
    }


}

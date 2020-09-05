package com.github.ericliucn.invfly;

import com.github.ericliucn.invfly.data.GsonTypes;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.data.SyncData;
import com.github.ericliucn.invfly.event.LoadDataEvent;
import com.github.ericliucn.invfly.event.SaveDataEvent;
import com.github.ericliucn.invfly.managers.DatabaseManager;
import com.github.ericliucn.invfly.utils.Utils;
import com.google.gson.Gson;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncDataService {

    private static final Gson gson = new Gson();
    private final List<Class<? extends SyncData>> classList = new ArrayList<>();


    public SyncDataService(){

    }

    public void loadUerData(User user) throws InstantiationException, IllegalAccessException {

        StorageData storageData = getDatabaseManager().getLatest(user);
        if (storageData != null) {
            new LoadDataEvent.Pre(user, storageData);
            this.loadUserData(user, storageData);
        }
        new LoadDataEvent.Done(user, storageData);

    }

    public void saveUserData(User user, boolean isDisconnect) throws IllegalAccessException, InstantiationException {
        Map<String, String> all = new HashMap<>();
        for (Class<? extends SyncData> dataClass : classList) {
            SyncData instance = dataClass.newInstance();
            try {
                String strData = instance.getSerializedData(user);
                all.put(instance.getID(), strData);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        StorageData storageData = new StorageData(user, gson.toJson(all, GsonTypes.ALLDATATYPE), isDisconnect);
        getDatabaseManager().saveData(storageData);
        new SaveDataEvent(user, storageData);
    }

    public void loadUserData(User user, StorageData data) throws IllegalAccessException, InstantiationException {
        new LoadDataEvent.Pre(user, data);
        Map<String, String> all = gson.fromJson(data.getData(), GsonTypes.ALLDATATYPE);
        for (Class<? extends SyncData> dataClass : classList) {
            SyncData instance = dataClass.newInstance();
            if (all.containsKey(instance.getID())) {
                String strData = all.get(instance.getID());
                instance.deserialize(user, strData);
                new LoadDataEvent.Done(user, data);
            } else {
                Text text = Utils.toText(Invfly.instance.getConfigLoader().getMessage().noValue).replace("%data%", Utils.toText(instance.getID()));
                Sponge.getServer().getConsole().sendMessage(text);
            }
        }
        new LoadDataEvent.Done(user, data);
    }

    public void register(Class<? extends SyncData> dataClass){
        if (!this.classList.contains(dataClass)){
            this.classList.add(dataClass);
        }
    }

    public void unregister(Class<? extends SyncData> dataClass){
        this.classList.remove(dataClass);
    }

    public void unregisterAll(){
        this.classList.clear();
    }

    private DatabaseManager getDatabaseManager(){
        return Invfly.instance.getDatabaseManager();
    }




}

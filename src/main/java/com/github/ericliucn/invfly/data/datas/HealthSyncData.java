package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.data.GsonTypes;
import com.github.ericliucn.invfly.data.SyncData;
import com.google.gson.Gson;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;

import java.util.HashMap;
import java.util.Map;

public class HealthSyncData implements SyncData {

    public static Gson gson = new Gson();

    @Override
    public String getSerializedData(User user) {
        Map<String, Double> doubleMap = new HashMap<>();
        double max = user.get(Keys.MAX_HEALTH).orElse(20D);
        double heal = user.get(Keys.HEALTH).orElse(20D);
        doubleMap.put("heal", heal);
        doubleMap.put("max", max);
        return gson.toJson(doubleMap, GsonTypes.HEAL);
    }

    @Override
    public void deserialize(User user, String data) {
        Map<String, Double> doubleMap = gson.fromJson(data, GsonTypes.HEAL);
        double heal = doubleMap.get("heal");
        double max = doubleMap.get("max");
        user.offer(Keys.HEALTH, heal);
        user.offer(Keys.MAX_HEALTH, max);
    }

    @Override
    public String getID() {
        return "health";
    }

    //Heal data must in main thread
    @Override
    public boolean shouldAsync() {
        return false;
    }

}

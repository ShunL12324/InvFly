package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.data.SyncData;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;

public class FoodSyncData implements SyncData {

    @Override
    public String getSerializedData(User user) {
        int food = user.get(Keys.FOOD_LEVEL).orElse(20);
        return String.valueOf(food);
    }

    @Override
    public void deserialize(User user, String data) {
        int food = Integer.parseInt(data);
        user.offer(Keys.FOOD_LEVEL, food);
    }

    @Override
    public String getID() {
        return "feedData";
    }
}

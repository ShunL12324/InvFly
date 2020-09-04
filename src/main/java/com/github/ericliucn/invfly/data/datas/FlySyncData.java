package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.data.SyncData;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;

public class FlySyncData implements SyncData {
    @Override
    public String getSerializedData(User user) {
        boolean canFly = user.get(Keys.CAN_FLY).orElse(false);
        boolean isFlying = user.get(Keys.IS_FLYING).orElse(false);
        double flySpeed = user.get(Keys.FLYING_SPEED).orElse(1D);
        return canFly + "," + isFlying + "," + flySpeed;
    }

    @Override
    public void deserialize(User user, String data) {
        String[] strings = data.split(",");
        user.offer(Keys.CAN_FLY, Boolean.valueOf(strings[0]));
        user.offer(Keys.IS_FLYING, Boolean.valueOf(strings[1]));
        user.offer(Keys.FLYING_SPEED, Double.parseDouble(strings[2]));
    }

    @Override
    public String getID() {
        return "flyData";
    }
}

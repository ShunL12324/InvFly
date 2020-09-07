package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.data.SyncData;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.entity.UserInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;

public class HotbarSyncData implements SyncData {

    @Override
    public String getSerializedData(User user) {
        UserInventory<User> inventory = user.getInventory().query(QueryOperationTypes.TYPE.of(UserInventory.class));
        int hotIndex = inventory.getHotbar().getSelectedSlotIndex();
        return String.valueOf(hotIndex);
    }

    @Override
    public void deserialize(User user, String data) {
        UserInventory<User> inventory = user.getInventory().query(QueryOperationTypes.TYPE.of(UserInventory.class));
        int hotIndex = Integer.parseInt(data);
        inventory.getHotbar().setSelectedSlotIndex(hotIndex);
    }

    @Override
    public String getID() {
        return "hotbar";
    }

    @Override
    public boolean shouldAsync() {
        return true;
    }

}

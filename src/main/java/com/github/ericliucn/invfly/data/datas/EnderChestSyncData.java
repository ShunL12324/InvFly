package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.data.GsonTypes;
import com.google.gson.Gson;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnderChestSyncData extends PlayerInvSyncData{

    private static final Gson gson = new Gson();

    @Override
    public String getSerializedData(User user) {
        Map<Integer, String> map = new HashMap<>();
        Inventory inventory = user.getEnderChestInventory();
        for (int i = 0; i < 27; i++) {
            Inventory slot = inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(i)));
            Optional<ItemStack> optionalItemStack = slot.peek();
            if (optionalItemStack.isPresent()){
                String data = ItemStackUtil.toNative(optionalItemStack.get()).serializeNBT().toString();
                map.put(i, data);
            }
        }
        return gson.toJson(map, GsonTypes.INVTYPE);
    }

    @Override
    public void deserialize(User user, String data) {
        this.deserialize(user.getEnderChestInventory(), gson.fromJson(data, GsonTypes.INVTYPE));
    }


    @Override
    public String getID() {
        return "enderchest";
    }
}

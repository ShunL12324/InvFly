package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.data.GsonTypes;
import com.github.ericliucn.invfly.data.SyncData;
import com.google.gson.Gson;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.HashMap;
import java.util.Map;

public class PlayerInvSyncData implements SyncData {

    private final Gson gson = new Gson();

    @Override
    public String getSerializedData(User user) {
        Map<Integer, String> map = new HashMap<>();
        user.getInventory().slots().forEach(inventory -> {
            inventory.peek().ifPresent(itemStack -> {
                inventory.getInventoryProperty(SlotIndex.class).ifPresent(slotIndex -> {
                    if (slotIndex.getValue() != null){
                        int index = slotIndex.getValue();
                        String data = ItemStackUtil.toNative(itemStack).serializeNBT().toString();
                        map.put(index, data);
                    }

                });
            });
        });
        return gson.toJson(map, GsonTypes.INVTYPE);
    }

    @Override
    public void deserialize(User user, String data) {

        Map<Integer, String> map = gson.fromJson(data, GsonTypes.INVTYPE);
        Inventory inventory = user.getInventory();
        this.deserialize(inventory, map);

    }

    protected void deserialize(Inventory inventory, Map<Integer, String> map){
        inventory.clear();
        map.forEach((key, value) -> {
            try {
                net.minecraft.item.ItemStack nativeItemStack = new net.minecraft.item.ItemStack(JsonToNBT.getTagFromJson(value));
                ItemStack itemStack = ItemStackUtil.fromNative(nativeItemStack);
                inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(key))).set(itemStack);
            } catch (NBTException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public String getID() {
        return "playerinventory";
    }
}

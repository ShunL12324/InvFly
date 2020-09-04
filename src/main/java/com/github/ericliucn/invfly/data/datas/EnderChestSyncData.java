package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.data.GsonTypes;
import com.google.gson.Gson;
import javafx.util.Pair;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnderChestSyncData extends PlayerInvSyncData{

    private final Gson gson = new Gson();

    @Override
    public String getSerializedData(User user) {
        List<Pair<Integer, String>> pairs = new ArrayList<>();
        Inventory inventory = user.getEnderChestInventory();
        for (int i = 0; i < 27; i++) {
            Inventory slot = inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(i)));
            Optional<ItemStack> optionalItemStack = slot.peek();
            if (optionalItemStack.isPresent()){
                try {
                    String data = DataFormats.JSON.write(optionalItemStack.get().toContainer());
                    pairs.add(new Pair<>(i, data));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return gson.toJson(pairs, GsonTypes.INVTYPE);
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

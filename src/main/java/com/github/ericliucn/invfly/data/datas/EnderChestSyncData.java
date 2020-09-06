package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.GsonTypes;
import com.github.ericliucn.invfly.event.DoneLoadEvent;
import com.github.ericliucn.invfly.utils.Utils;
import com.google.gson.Gson;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.*;

public class EnderChestSyncData extends PlayerInvSyncData{

    private static final Gson GSON = new Gson();
    public static final List<UUID> FREEZE = new ArrayList<>();

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
        return GSON.toJson(map, GsonTypes.INVTYPE);
    }

    @Override
    public void deserialize(User user, String data) {
        this.deserialize(user.getEnderChestInventory(), GSON.fromJson(data, GsonTypes.INVTYPE));
    }


    @Override
    public String getID() {
        return "enderchest";
    }

    @Override
    public boolean shouldRegListener() {
        return true;
    }

    @Listener
    public void onAuth(ClientConnectionEvent.Auth event){
        boolean shouldFreeze = Invfly.instance.getConfigLoader().getConfig().general.freezeInventory;
        if (shouldFreeze){
            FREEZE.add(event.getProfile().getUniqueId());
        }
    }

    @Listener
    public void onChangeInv(ChangeInventoryEvent event){
        Message message = Invfly.instance.getConfigLoader().getMessage();
        if ((event.getSource() instanceof Player) && (FREEZE.contains(((Player) event.getSource()).getUniqueId()))){
            event.setCancelled(true);
            ((Player) event.getSource()).sendMessage(Utils.toText(message.invLocked));
        }
    }

    @Listener
    public void onDoneLoad(DoneLoadEvent event){
        if (event.getSyncData().getID().equals(this.getID()) && event.getResult().equals(EnumResult.SUCCESS)){
            FREEZE.remove(event.getUuid());
        }
    }
}

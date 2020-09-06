package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.GsonTypes;
import com.github.ericliucn.invfly.data.SyncData;
import com.github.ericliucn.invfly.event.DoneLoadEvent;
import com.github.ericliucn.invfly.utils.Utils;
import com.google.gson.Gson;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
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

public class PlayerInvSyncData implements SyncData {

    private final static Gson gson = new Gson();
    public static final List<UUID> FREEZE = new ArrayList<>();

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

    @Override
    public boolean shouldAsync() {
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

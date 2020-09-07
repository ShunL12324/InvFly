package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.InvFlyConfig;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.GsonTypes;
import com.github.ericliucn.invfly.data.SyncData;
import com.github.ericliucn.invfly.event.LoadSingleEvent;
import com.github.ericliucn.invfly.managers.DatabaseManager;
import com.github.ericliucn.invfly.utils.Utils;
import com.google.gson.Gson;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.*;

public class PlayerInvSyncData implements SyncData {

    private final static Gson gson = new Gson();
    public static final List<UUID> FREEZE = new ArrayList<>();
    private SpongeExecutorService async;
    private InvFlyConfig config;
    private DatabaseManager databaseManager;

    public PlayerInvSyncData(){
        config = Invfly.instance.getConfigLoader().getConfig();
        async = Invfly.instance.getAsyncExecutor();
        databaseManager = Invfly.instance.getDatabaseManager();
    }

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

    @Override
    public boolean shouldRegListener() {
        return true;
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event, @First Player player){
        if (config.general.freezeInventory){
            FREEZE.add(player.getUniqueId());
        }
        async.submit(()->{
            if (!databaseManager.isDataExists(player.getUniqueId())){
                FREEZE.remove(player.getUniqueId());
            }
        });
    }

    @Listener
    public void onChangeInv(ChangeInventoryEvent event, @First Player player){
        Message message = Invfly.instance.getConfigLoader().getMessage();
        if (FREEZE.contains(player.getUniqueId())){
            event.setCancelled(true);
            player.sendMessage(Utils.toText(message.invLocked));
        }
    }

    @Listener
    public void onDoneLoad(LoadSingleEvent.Done event){
        if (event.getSyncData().getID().equals(this.getID()) && event.getResult().equals(EnumResult.SUCCESS)){
            FREEZE.remove(event.getTargetUser().getUniqueId());
        }
    }

}

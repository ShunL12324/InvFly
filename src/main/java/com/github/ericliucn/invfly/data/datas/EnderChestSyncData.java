package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.InvFlyConfig;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.GsonTypes;
import com.github.ericliucn.invfly.event.LoadSingleEvent;
import com.github.ericliucn.invfly.managers.DatabaseManager;
import com.github.ericliucn.invfly.utils.Utils;
import com.google.gson.Gson;
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
import java.util.concurrent.CopyOnWriteArrayList;

public class EnderChestSyncData extends PlayerInvSyncData{

    private static final Gson GSON = new Gson();
    public static final List<UUID> FREEZE = new CopyOnWriteArrayList<>();
    private SpongeExecutorService async;
    private InvFlyConfig config;
    private DatabaseManager databaseManager;

    public EnderChestSyncData(){
        config = Invfly.instance.getConfigLoader().getConfig();
        async = Invfly.instance.getAsyncExecutor();
        databaseManager = Invfly.instance.getDatabaseManager();
    }

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

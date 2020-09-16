package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.InvFlyConfig;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.event.LoadSingleEvent;
import com.github.ericliucn.invfly.exception.DeserializeException;
import com.github.ericliucn.invfly.service.SyncDataService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EnderChestSyncData extends PlayerInvSyncData{

    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<List<String>>(){}.getType();
    private final InvFlyConfig config;
    private final Message message;
    private final SyncDataService service;
    public static final Map<UUID, Future<Boolean>> FREEZE = new ConcurrentHashMap<>();

    public EnderChestSyncData(){
        config = Invfly.instance.getConfigLoader().getConfig();
        message = Invfly.instance.getConfigLoader().getMessage();
        service = Invfly.instance.getService();
    }

    @Override
    public String getSerializedData(User user) {
        List<String> list = new ArrayList<>();
        InventoryEnderChest enderChest = (InventoryEnderChest) user.getEnderChestInventory();
        for (NBTBase tag:enderChest.saveInventoryToNBT()){
            list.add(tag.toString());
        }
        return GSON.toJson(list, TYPE);
    }

    @Override
    public void deserialize(User user, String data) throws DeserializeException {
        InventoryEnderChest enderChest = (InventoryEnderChest) user.getEnderChestInventory();
        try {
            enderChest.loadInventoryFromNBT(deserializeInvNBTTagList(data));
        } catch (NBTException e) {
            e.printStackTrace();
            throw new DeserializeException(this.getID());
        }
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
        if (config.general.lockInv){
            UUID userUUID = event.getProfile().getUniqueId();
            FREEZE.put(userUUID, service.getExists(userUUID));
        }
    }

    @Listener
    public void onChangeInv(ChangeInventoryEvent event, @First Player player){
        UUID uuid = player.getUniqueId();
        Future<Boolean> isExists = FREEZE.get(uuid);
        if (!FREEZE.containsKey(uuid)) return;
        if (!player.hasPermission("invfly.sync.auto.load")) return;
        try {
            if (isExists.isDone() && !isExists.get()){
                FREEZE.remove(uuid);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (FREEZE.containsKey(uuid)){
            event.setCancelled(true);
            player.sendMessage(message.getMessage("info.lock.inv"));
        }
    }

    @Listener
    public void onDoneLoad(LoadSingleEvent.Done event){
        if (event.getSyncData().getID().equals(this.getID()) && event.getResult().equals(EnumResult.SUCCESS)){
            FREEZE.remove(event.getTargetUser().getUniqueId());
        }
    }
}

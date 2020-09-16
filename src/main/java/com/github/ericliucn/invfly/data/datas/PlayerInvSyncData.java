package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.api.SyncData;
import com.github.ericliucn.invfly.config.InvFlyConfig;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.event.LoadSingleEvent;
import com.github.ericliucn.invfly.exception.DeserializeException;
import com.github.ericliucn.invfly.service.SyncDataService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagList;
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

public class PlayerInvSyncData implements SyncData {

    private final static Gson GSON = new Gson();
    public static final Map<UUID, Future<Boolean>> FREEZE = new ConcurrentHashMap<>();
    private final SyncDataService service;
    private final InvFlyConfig config;
    private final Message message;
    private static final Type TYPE = new TypeToken<List<String>>(){}.getType();

    public PlayerInvSyncData(){
        config = Invfly.instance.getConfigLoader().getConfig();
        service = Invfly.instance.getService();
        message = Invfly.instance.getConfigLoader().getMessage();
    }

    @Override
    public String getSerializedData(User user) {
        List<String> list = new ArrayList<>();
        InventoryPlayer inventoryPlayer = (InventoryPlayer) user.getInventory();
        NBTTagList tagList = inventoryPlayer.writeToNBT(new NBTTagList());
        for (NBTBase tagCompound:tagList){
            list.add(tagCompound.toString());
        }
        return GSON.toJson(list, TYPE);
    }


    @Override
    public void deserialize(User user, String data) throws DeserializeException {
        InventoryPlayer inventory = (InventoryPlayer) user.getInventory();
        try {
            inventory.readFromNBT(deserializeInvNBTTagList(data));
        } catch (NBTException e) {
            e.printStackTrace();
            throw new DeserializeException(this.getID());
        }
    }

    protected NBTTagList deserializeInvNBTTagList(String string) throws NBTException {
        List<String> list = GSON.fromJson(string, TYPE);
        NBTTagList nbtTagList = new NBTTagList();
        for (String tag:list){
            nbtTagList.appendTag(JsonToNBT.getTagFromJson(tag));
        }
        return nbtTagList;
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

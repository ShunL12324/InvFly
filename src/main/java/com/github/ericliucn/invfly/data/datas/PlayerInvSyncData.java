package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.InvFlyConfig;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.api.SyncData;
import com.github.ericliucn.invfly.event.LoadSingleEvent;
import com.github.ericliucn.invfly.exception.DeserializeException;
import com.github.ericliucn.invfly.managers.DatabaseManager;
import com.github.ericliucn.invfly.utils.Utils;
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
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerInvSyncData implements SyncData {

    private final static Gson GSON = new Gson();
    public static final List<UUID> FREEZE = new ArrayList<>();
    private SpongeExecutorService async;
    private InvFlyConfig config;
    private DatabaseManager databaseManager;
    private static final Type TYPE = new TypeToken<List<String>>(){}.getType();

    public PlayerInvSyncData(){
        config = Invfly.instance.getConfigLoader().getConfig();
        async = Invfly.instance.getAsyncExecutor();
        databaseManager = Invfly.instance.getDatabaseManager();
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
    public void onJoin(ClientConnectionEvent.Join event, @First Player player){
        if (config.general.freezeInventory) FREEZE.add(player.getUniqueId());
        async.submit(()->{ if (!databaseManager.isDataExists(player.getUniqueId())) FREEZE.remove(player.getUniqueId()); });
    }

    @Listener
    public void onChangeInv(ChangeInventoryEvent event, @First Player player){
        Message message = Invfly.instance.getConfigLoader().getMessage();
        if (FREEZE.contains(player.getUniqueId())){
            event.setCancelled(true);
            player.sendMessage(message.getMessage("info.lock.inventory"));
        }
    }

    @Listener
    public void onDoneLoad(LoadSingleEvent.Done event){
        if (event.getSyncData().getID().equals(this.getID()) && event.getResult().equals(EnumResult.SUCCESS)){
            FREEZE.remove(event.getTargetUser().getUniqueId());
        }
    }

}

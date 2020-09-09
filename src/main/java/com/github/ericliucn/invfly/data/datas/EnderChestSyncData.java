package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.InvFlyConfig;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.event.LoadSingleEvent;
import com.github.ericliucn.invfly.exception.DeserializeException;
import com.github.ericliucn.invfly.utils.Utils;
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
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EnderChestSyncData extends PlayerInvSyncData{

    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<List<String>>(){}.getType();
    private final SpongeExecutorService async;
    private final InvFlyConfig config;

    public EnderChestSyncData(){
        config = Invfly.instance.getConfigLoader().getConfig();
        async = Invfly.instance.getAsyncExecutor();
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
    public void onJoin(ClientConnectionEvent.Join event, @First Player player){
        if (config.general.freezeInventory){
            FREEZE.add(player.getUniqueId());
        }

    }


    @Listener
    public void onChangeInv(ChangeInventoryEvent event, @First Player player){
        Message message = Invfly.instance.getConfigLoader().getMessage();
        if (FREEZE.contains(player.getUniqueId())){
            event.setCancelled(true);
        }
    }


    @Listener
    public void onDoneLoad(LoadSingleEvent.Done event){
        if (event.getSyncData().getID().equals(this.getID()) && event.getResult().equals(EnumResult.SUCCESS)){
            FREEZE.remove(event.getTargetUser().getUniqueId());
        }
    }
}

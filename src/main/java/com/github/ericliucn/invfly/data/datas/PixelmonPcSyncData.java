package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.data.SyncData;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.entity.living.player.User;

import java.util.UUID;

public class PixelmonPcSyncData implements SyncData {

    @Override
    public String getSerializedData(User user) {
        UUID uuid = user.getUniqueId();
        PCStorage pcStorage = Pixelmon.storageManager.getPCForPlayer(uuid);
        return pcStorage.writeToNBT(new NBTTagCompound()).toString();
    }

    @Override
    public void deserialize(User user, String data) {
        UUID uuid = user.getUniqueId();
        try {
            Pixelmon.storageManager.getPCForPlayer(uuid).readFromNBT(JsonToNBT.getTagFromJson(data));
        } catch (NBTException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getID() {
        return "pixelmonPc";
    }
}

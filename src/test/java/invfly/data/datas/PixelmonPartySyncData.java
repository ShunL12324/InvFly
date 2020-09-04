package invfly.data.datas;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.storage.IStorageManager;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import invfly.data.SyncData;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.entity.living.player.User;

import java.util.UUID;

public class PixelmonPartySyncData implements SyncData {

    @Override
    public String getSerializedData(User user) {
        UUID uuid = user.getUniqueId();
        IStorageManager manager = Pixelmon.storageManager;
        PartyStorage partyStorage = manager.getParty(uuid);
        return partyStorage.writeToNBT(new NBTTagCompound()).toString();
    }

    @Override
    public void deserialize(User user, String data) {
        try {
            NBTTagCompound tagCompound = JsonToNBT.getTagFromJson(data);
            PartyStorage partyStorage = new PartyStorage(user.getUniqueId());
            partyStorage.readFromNBT(tagCompound);
        } catch (NBTException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getID() {
        return "pixelmonParty";
    }

}

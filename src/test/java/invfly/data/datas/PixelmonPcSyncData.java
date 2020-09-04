package invfly.data.datas;

import invfly.data.SyncData;
import org.spongepowered.api.entity.living.player.User;

public class PixelmonPcSyncData implements SyncData {

    @Override
    public String getSerializedData(User user) {
        return null;
    }

    @Override
    public void deserialize(User user, String data) {

    }

    @Override
    public String getID() {
        return null;
    }
}

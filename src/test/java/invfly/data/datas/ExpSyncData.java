package invfly.data.datas;

import invfly.data.SyncData;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;

public class ExpSyncData implements SyncData {

    @Override
    public String getSerializedData(User user) {
        int exp = user.get(Keys.TOTAL_EXPERIENCE).orElse(0);
        return String.valueOf(exp);
    }

    @Override
    public void deserialize(User user, String data) {
        user.offer(Keys.TOTAL_EXPERIENCE, Integer.parseInt(data));
    }


    @Override
    public String getID() {
        return "experience";
    }
}

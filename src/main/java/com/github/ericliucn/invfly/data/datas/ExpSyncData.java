package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.api.SyncData;
import com.github.ericliucn.invfly.exception.DeserializeException;
import com.github.ericliucn.invfly.exception.SerializeException;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;

public class ExpSyncData implements SyncData {


    @Override
    public String getSerializedData(User user) throws SerializeException {

        Optional<Integer> optionalExp = user.get(Keys.TOTAL_EXPERIENCE);
        if (!optionalExp.isPresent()){
            throw new SerializeException(this.getID());
        }
        return String.valueOf(optionalExp.get());
    }

    @Override
    public void deserialize(User user, String data) throws DeserializeException {
        if (data == null) throw new DeserializeException(this.getID());
        DataTransactionResult result = user.offer(Keys.TOTAL_EXPERIENCE, Integer.parseInt(data));
        if (!result.isSuccessful()) throw new DeserializeException(getID());
    }


    @Override
    public String getID() {
        return "experience";
    }

    @Override
    public boolean shouldAsync() {
        return true;
    }

}

package com.github.ericliucn.invfly.data;

import org.spongepowered.api.entity.living.player.User;

public interface SyncData {

    /**
     * The method to serialize data
     * @param user The user
     * @return serialized data
     */
    String getSerializedData(User user);

    /**
     * Deserialize data to user
     * @param user The user
     * @param data serialized data
     */
    void deserialize(User user, String data);

    /**
     * @return A unique string id
     */
    String getID();

}

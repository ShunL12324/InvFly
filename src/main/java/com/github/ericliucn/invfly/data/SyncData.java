package com.github.ericliucn.invfly.data;

import com.github.ericliucn.invfly.event.DoneLoadEvent;
import com.github.ericliucn.invfly.event.PreLoadDataEvent;
import com.github.ericliucn.invfly.exception.DeserializeException;
import com.github.ericliucn.invfly.exception.SerializeException;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public interface SyncData {

    /**
     * The method to serialize data
     * @param user The user
     * @return serialized data
     */
    String getSerializedData(User user) throws SerializeException;

    /**
     * Deserialize data to user
     * @param user The user
     * @param data serialized data
     */
    void deserialize(User user, String data) throws DeserializeException;

    /**
     * @return A unique string id
     */
    String getID();


    /**default use id as permission node
     * @return Unique permission node
     */
    default String getPermissionNode(){
        return "invfly.sync." + getID();
    }

    /**
     * @return should async?
     */
    boolean shouldAsync();


    default boolean shouldRegListener(){
        return false;
    }

}

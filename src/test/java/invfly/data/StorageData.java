package invfly.data;

import invfly.Invfly;
import org.spongepowered.api.entity.living.player.User;

import java.sql.Timestamp;

public class StorageData {

    private final String name;
    private final String uuid;
    private final String data;
    private final boolean disconnect;
    private final Timestamp time;
    private int id = -1;
    private String serverName = Invfly.instance.getConfigLoader().getConfig().general.serverName;

    public StorageData(User user, String data, Timestamp timestamp, boolean disconnect){
        this.data = data;
        this.name = user.getName();
        this.uuid = user.getUniqueId().toString();
        this.disconnect = disconnect;
        this.time = timestamp;
    }

    public StorageData(User user, String data, Timestamp timestamp){
        this(user, data, timestamp, false);
    }

    public StorageData(User user, String data, boolean disconnect){
        this(user, data, new Timestamp(System.currentTimeMillis()), disconnect);
    }

    public StorageData(User user, String data){
        this(user, data, new Timestamp(System.currentTimeMillis()));
    }

    public StorageData(int id, String uuid, String name, String data, Timestamp timestamp, boolean disconnect, String serverName){
        this.time = timestamp;
        this.name = name;
        this.uuid = uuid;
        this.disconnect = disconnect;
        this.data = data;
        this.id = id;
        this.serverName = serverName;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public String getUuid() {
        return uuid;
    }

    public Timestamp getTime() {
        return time;
    }

    public boolean isDisconnect() {
        return disconnect;
    }

    public int getId() {
        return id;
    }

    public String getServerName() {
        if (this.serverName == null){
            return "UNKNOWN";
        }
        return this.serverName;
    }
}

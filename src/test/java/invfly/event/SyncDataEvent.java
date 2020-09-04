package invfly.event;

import invfly.data.StorageData;
import org.spongepowered.api.event.user.TargetUserEvent;

public interface SyncDataEvent extends TargetUserEvent {

    StorageData getStorageData();
}

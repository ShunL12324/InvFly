package invfly.event;

import invfly.Invfly;
import invfly.data.StorageData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.plugin.PluginContainer;

/**
 * Post when load data finish
 */
public class LoadDataEvent {


    public static class Pre implements SyncDataEvent {

        private final User user;
        private final StorageData storageData;
        private final PluginContainer container;

        public Pre(User user, StorageData storageData){
            this.container = Invfly.instance.getPluginContainer();
            this.user = user;
            this.storageData = storageData;
            Sponge.getEventManager().post(this);
        }

        @Override
        public Cause getCause() {
            EventContext context = EventContext.builder().add(EventContextKeys.PLUGIN, container).build();
            return Cause.builder().append(Invfly.instance).build(context);
        }

        @Override
        public Object getSource() {
            return user;
        }

        @Override
        public EventContext getContext() {
            return EventContext.builder().add(EventContextKeys.PLUGIN, container).build();
        }

        @Override
        public StorageData getStorageData() {
            return storageData;
        }

        @Override
        public User getTargetUser() {
            return user;
        }
    }

    public static class Done implements SyncDataEvent {

        private final User user;
        private final StorageData storageData;
        private final PluginContainer container = Invfly.instance.getPluginContainer();

        public Done(User user, StorageData storageData){
            this.user = user;
            this.storageData = storageData;
            Sponge.getEventManager().post(this);
        }

        @Override
        public Cause getCause() {
            EventContext context = EventContext.builder().add(EventContextKeys.PLUGIN, container).build();
            return Cause.builder().append(Invfly.instance).build(context);
        }

        @Override
        public Object getSource() {
            return user;
        }

        @Override
        public EventContext getContext() {
            return EventContext.builder().add(EventContextKeys.PLUGIN, container).build();
        }

        @Override
        public StorageData getStorageData() {
            return storageData;
        }

        @Override
        public User getTargetUser() {
            return user;
        }
    }

}

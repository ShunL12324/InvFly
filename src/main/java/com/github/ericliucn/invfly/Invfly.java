package com.github.ericliucn.invfly;

import com.github.ericliucn.invfly.config.ConfigLoader;
import com.github.ericliucn.invfly.config.InvFlyConfig;
import com.github.ericliucn.invfly.data.datas.*;
import com.github.ericliucn.invfly.managers.CommandManager;
import com.github.ericliucn.invfly.managers.DatabaseManager;
import com.github.ericliucn.invfly.managers.EventHandler;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.io.File;

@Plugin(
        id = "invfly",
        name = "Invfly",
        description = "InvFly",
        authors = {
                "EricLiu"
        }
)
public class Invfly {

    @Inject @ConfigDir(sharedRoot = false) private File file;
    @Inject private Logger logger;
    @Inject private PluginContainer pluginContainer;

    public static Invfly instance;
    private ConfigLoader configLoader;
    private DatabaseManager databaseManager;
    private CommandManager commandManager;
    private SyncDataService service;
    private EventHandler eventHandler;
    private SpongeExecutorService asyncExecutor;
    private SpongeExecutorService syncExecutor;

    @Listener
    public void onPostInit(GamePostInitializationEvent event){
        instance = this;
        asyncExecutor = Sponge.getScheduler().createAsyncExecutor(this);
        syncExecutor = Sponge.getScheduler().createSyncExecutor(this);
        configLoader = new ConfigLoader(file);
        databaseManager = new DatabaseManager();
        Sponge.getServiceManager().setProvider(this, SyncDataService.class, new SyncDataService());
        service = Sponge.getServiceManager().provideUnchecked(SyncDataService.class);
        registerModule();
        eventHandler = new EventHandler();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        commandManager = new CommandManager();
    }

    @Listener
    public void onReload(GameReloadEvent event){
        this.reload();
    }


    public void reload(){
        eventHandler.unregister();
        service.unregisterAll();
        configLoader = new ConfigLoader(file);
        databaseManager = new DatabaseManager();
        registerModule();
        eventHandler.register();
    }

    private void registerModule(){
        InvFlyConfig.Module module = configLoader.getConfig().module;
        if (module.enderChestInventory){
            service.register(EnderChestSyncData.class);
        }
        if (module.playerMainInventory){
            service.register(PlayerInvSyncData.class);
        }
        if (module.experience){
            service.register(ExpSyncData.class);
        }
        if (module.gameMode){
            service.register(GameModeSyncData.class);
        }
        if (module.health){
            service.register(HealthSyncData.class);
        }
        if (module.hotbarIndex){
            service.register(HotbarSyncData.class);
        }
        if (module.potion){
            service.register(PotionSyncData.class);
        }
        if (module.food){
            service.register(FoodSyncData.class);
        }
        if (module.flyData){
            service.register(FlySyncData.class);
        }
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public Logger getLogger() {
        return logger;
    }

    public SyncDataService getService() {
        return service;
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    public SpongeExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public SpongeExecutorService getSyncExecutor() {
        return syncExecutor;
    }
}

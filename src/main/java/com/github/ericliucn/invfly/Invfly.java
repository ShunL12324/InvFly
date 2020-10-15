package com.github.ericliucn.invfly;

import com.github.ericliucn.invfly.config.ConfigLoader;
import com.github.ericliucn.invfly.config.InvFlyConfig;
import com.github.ericliucn.invfly.data.datas.*;
import com.github.ericliucn.invfly.managers.CommandManager;
import com.github.ericliucn.invfly.managers.DatabaseManager;
import com.github.ericliucn.invfly.managers.EventHandler;
import com.github.ericliucn.invfly.service.SyncDataService;
import com.google.gson.Gson;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.user.UserStorageService;

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
    private SyncDataService service;
    private EventHandler eventHandler;
    private SpongeExecutorService asyncExecutor;
    private SpongeExecutorService syncExecutor;
    private UserStorageService userStorageService;
    private Gson GSON;

    @Listener
    public void onPostInit(GamePostInitializationEvent event){
        instance = this;
        configLoader = new ConfigLoader(file);
        Sponge.getServiceManager().setProvider(this, SyncDataService.class, new SyncDataService());
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        new CommandManager();
        databaseManager = new DatabaseManager();
        GSON = new Gson();
        asyncExecutor = Sponge.getScheduler().createAsyncExecutor(this);
        syncExecutor = Sponge.getScheduler().createSyncExecutor(this);
        userStorageService = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        service = Sponge.getServiceManager().provideUnchecked(SyncDataService.class);
        registerModule();
        eventHandler = new EventHandler();
    }

    @Listener
    public void onServerStop(GameStoppingEvent event){
        Sponge.getServer().getOnlinePlayers().forEach(player -> {
            service.saveUserData(player, true, Sponge.getServer().getConsole());
        });
    }

    @Listener
    public void onReload(GameReloadEvent event){
        this.reload();
    }


    public void reload(){
        if (eventHandler != null) {
            eventHandler.unregister();
        }else {
            eventHandler = new EventHandler();
        }
        if (service != null) {
            service.unregisterAll();
        }else {
            service = Sponge.getServiceManager().provideUnchecked(SyncDataService.class);
        }
        configLoader = new ConfigLoader(file);
        databaseManager = new DatabaseManager();
        registerModule();
        eventHandler.register();
    }

    private void registerModule(){
        InvFlyConfig.Module module = configLoader.getConfig().module;
        if (module.enderChestInventory){
            service.register(new EnderChestSyncData());
        }
        if (module.playerMainInventory){
            service.register(new PlayerInvSyncData());
        }
        if (module.experience){
            service.register(new ExpSyncData());
        }
        if (module.gameMode){
            service.register(new GameModeSyncData());
        }
        if (module.health){
            service.register(new HealthSyncData());
        }
        if (module.hotbarIndex){
            service.register(new HotbarSyncData());
        }
        if (module.potion){
            service.register(new PotionSyncData());
        }
        if (module.food){
            service.register(new FoodSyncData());
        }
        if (module.flyData){
            service.register(new FlySyncData());
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

    public UserStorageService getUserStorageService() {
        return userStorageService;
    }

    public Gson getGSON() {
        return GSON;
    }
}

package com.github.ericliucn.invfly.managers;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.InvFlyConfig;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.event.LoadSingleEvent;
import com.github.ericliucn.invfly.service.SyncDataService;
import com.github.ericliucn.invfly.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EventHandler {

    private SyncDataService service;
    private DatabaseManager databaseManager;
    private SpongeExecutorService asyncExecutor;
    private Message message;
    private InvFlyConfig config;

    public EventHandler(){
        this.register();
    }

    public void register(){
        Sponge.getEventManager().registerListeners(Invfly.instance, this);
        service = Invfly.instance.getService();
        databaseManager = Invfly.instance.getDatabaseManager();
        asyncExecutor = Invfly.instance.getAsyncExecutor();
        config = Invfly.instance.getConfigLoader().getConfig();
        message = Invfly.instance.getConfigLoader().getMessage();
    }

    public void unregister(){
        Sponge.getEventManager().unregisterListeners(this);
    }


    @Listener
    public void worldSave(SaveWorldEvent.Post event){
        if (config.general.saveWhenWorldSave) {
            World world = event.getTargetWorld();
            for (Player player : world.getPlayers()) {
                if (player.hasPermission("invfly.sync.auto")) {
                    asyncExecutor.submit(() -> service.saveUserData(player, false));
                }
            }
        }
        asyncExecutor.submit(()-> databaseManager.deleteOutDate(config.general.outDate));
    }


    @Listener
    public void onJoin(ClientConnectionEvent.Join event, @First Player player){
        if (player.hasPermission("invfly.sync.auto")){
            asyncExecutor.schedule(()->{
                StorageData storageData = databaseManager.getLatest(player);
                if (storageData != null){
                    // first join
                    if (!storageData.isDisconnect()){
                        // wrong data
                        player.sendMessage(Utils.toText(message.retry).replace("%second%", Text.of(config.general.nextRetryTime)));
                        if (config.general.retryTimes > 0) {
                            asyncExecutor.scheduleAtFixedRate(new Retry(player), config.general.nextRetryTime, config.general.nextRetryTime, TimeUnit.SECONDS);
                        }
                    }else {
                        //right data
                        service.loadUserData(player, storageData, true);
                    }
                }
            }, config.general.initialDelayWhenJoin, TimeUnit.MILLISECONDS);
        }
    }

    @Listener
    public void disconnect(ClientConnectionEvent.Disconnect event, @First Player player){
        if (player.hasPermission("invfly.sync.auto")) {
            asyncExecutor.submit(() -> service.saveUserData(player, true));
        }
    }

    private class Retry extends TimerTask{
        private int count;
        private final Player player;

        public Retry(Player player){
            this.count = 0;
            this.player = player;
        }

        @Override
        public void run() {
            StorageData data = databaseManager.getLatest(player);
            if (data.isDisconnect()){
                service.loadUserData(player, data, true);
                this.cancel();
            }else {
                if (count < config.general.retryTimes){
                    player.sendMessage(Utils.toText(message.retry).replace("%second%", Text.of(config.general.nextRetryTime)));
                }else if (count == config.general.retryTimes){
                    if (!config.general.preventDirtyData){
                        player.sendMessage(Utils.toText(message.retryFailAndTryToLoadLatestData));
                        service.loadUserData(player, data, true);
                    }else {
                        player.sendMessage(Utils.toText(message.finallyFailToLoad));
                    }
                    this.cancel();
                }else {
                    this.cancel();
                }
            }
            count += 1;
        }
    }


}

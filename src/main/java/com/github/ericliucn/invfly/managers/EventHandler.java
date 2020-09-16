package com.github.ericliucn.invfly.managers;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.api.LoadAllEvent;
import com.github.ericliucn.invfly.config.InvFlyConfig;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.service.SyncDataService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

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
        asyncExecutor.submit(()-> databaseManager.deleteOutDate(config.general.outDate));
    }


    @Listener
    public void onJoin(ClientConnectionEvent.Join event, @First Player player){
        if (player.hasPermission("invfly.sync.auto.load")){
            asyncExecutor.schedule(()->{
                StorageData storageData = databaseManager.getLatest(player);
                if (storageData != null){
                    // first join
                    if (!storageData.isDisconnect()){
                        // wrong data
                        player.sendMessage(message.getMessage("info.retry").replace("%second%", Text.of(config.general.nextRetryTime)));
                        if (config.general.retryTimes > 0) {
                            asyncExecutor.scheduleAtFixedRate(new Retry(player), config.general.nextRetryTime, config.general.nextRetryTime, TimeUnit.SECONDS);
                        }
                    }else {
                        //right data
                        service.loadUserData(player, storageData, true, player);
                    }
                }
            }, config.general.initialDelayWhenJoin, TimeUnit.MILLISECONDS);
        }
    }

    @Listener
    public void disconnect(ClientConnectionEvent.Disconnect event, @First Player player){
        if (player.hasPermission("invfly.sync.auto.save")) {
            asyncExecutor.submit(() -> service.saveUserData(player, true, null));
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
                service.loadUserData(player, data, true, player);
                this.cancel();
            }else {
                if (count < config.general.retryTimes){
                    player.sendMessage(message.getMessage("info.retry").replace("%second%", Text.of(config.general.nextRetryTime)));
                }else if (count == config.general.retryTimes){
                    if (!config.general.preventDirtyData){
                        player.sendMessage(message.getMessage("info.failed.trylatest"));
                        service.loadUserData(player, data, true, player);
                    }else {
                        player.sendMessage(message.getMessage("info.failed.finally"));
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

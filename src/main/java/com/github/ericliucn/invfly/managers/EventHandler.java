package com.github.ericliucn.invfly.managers;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.EnumResult;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.data.datas.EnderChestSyncData;
import com.github.ericliucn.invfly.data.datas.PlayerInvSyncData;
import com.github.ericliucn.invfly.event.DoneLoadEvent;
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

    private final static List<UUID> BLACKLIST = new ArrayList<>();

    private SyncDataService service;
    private DatabaseManager databaseManager;
    private SpongeExecutorService asyncExecutor;
    private int retry;
    private long delay;
    private int nextRetry;
    private boolean preventDirtyData;
    private Duration outDate;
    private Message message;
    private boolean saveWhenWorldSave;

    public EventHandler(){
        this.register();
    }

    public void register(){
        Sponge.getEventManager().registerListeners(Invfly.instance, this);
        service = Invfly.instance.getService();
        databaseManager = Invfly.instance.getDatabaseManager();
        asyncExecutor = Invfly.instance.getAsyncExecutor();
        retry = Invfly.instance.getConfigLoader().getConfig().general.retry;
        delay = Invfly.instance.getConfigLoader().getConfig().general.delay;
        nextRetry = Invfly.instance.getConfigLoader().getConfig().general.nextRetry;
        preventDirtyData = Invfly.instance.getConfigLoader().getConfig().general.preventDirtyData;
        outDate = Invfly.instance.getConfigLoader().getConfig().general.outDate;
        message = Invfly.instance.getConfigLoader().getMessage();
        saveWhenWorldSave = Invfly.instance.getConfigLoader().getConfig().general.saveWhenWorldSave;
    }

    public void unregister(){
        Sponge.getEventManager().unregisterListeners(this);
    }

    @Listener
    public void worldSave(SaveWorldEvent.Post event){
        if (!saveWhenWorldSave) return;
        World world = event.getTargetWorld();
        for (Player player : world.getPlayers()) {
            if (!BLACKLIST.contains(player.getUniqueId()) && player.hasPermission("invfly.sync.auto")) {
                asyncExecutor.submit(() -> service.saveUserData(player, false));
            }
        }
        asyncExecutor.submit(()-> databaseManager.deleteOutDate(outDate));
    }

    @Listener
    public void onAuth(ClientConnectionEvent.Auth event){
        BLACKLIST.add(event.getProfile().getUniqueId());
    }


    @Listener
    public void onJoin(ClientConnectionEvent.Join event, @First Player player){
        if (player.hasPermission("invfly.sync.auto")){
            asyncExecutor.schedule(()->{
                StorageData storageData = databaseManager.getLatest(player);
                if (storageData == null){
                    // first join
                    new DoneLoadEvent(player, storageData, EnumResult.SUCCESS, new PlayerInvSyncData());
                    new DoneLoadEvent(player, storageData, EnumResult.SUCCESS, new EnderChestSyncData());
                    BLACKLIST.remove(player.getUniqueId());
                }else if (!storageData.isDisconnect()){
                    // wrong data
                    player.sendMessage(Utils.toText(message.retry).replace("%second%", Text.of(nextRetry)));
                    if (retry > 0) {
                        asyncExecutor.scheduleAtFixedRate(new Retry(player), nextRetry, nextRetry, TimeUnit.MILLISECONDS);
                    }
                }else {
                    service.loadUserData(player, storageData, true);
                    BLACKLIST.remove(player.getUniqueId());
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    @Listener
    public void disconnect(ClientConnectionEvent.Disconnect event, @First Player player){
        if (player.hasPermission("invfly.sync.auto") && !BLACKLIST.contains(player.getUniqueId())) {
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
                BLACKLIST.remove(player.getUniqueId());
                this.cancel();
            }else {
                if (count < retry){
                    player.sendMessage(Utils.toText(message.retry).replace("%second%", Text.of(nextRetry)));
                }else {
                    if (!preventDirtyData){
                        player.sendMessage(Utils.toText(message.retryFailAndTryToLoadLatestData));
                        service.loadUserData(player, data, true);
                        BLACKLIST.remove(player.getUniqueId());
                        this.cancel();
                    }else {
                        player.sendMessage(Utils.toText(message.finallyFailToLoad));
                        this.cancel();
                    }
                }
            }
            count += 1;
        }
    }


}

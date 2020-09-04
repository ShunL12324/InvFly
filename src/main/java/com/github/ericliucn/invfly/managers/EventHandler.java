package com.github.ericliucn.invfly.managers;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.SyncDataService;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.event.LoadDataEvent;
import com.github.ericliucn.invfly.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class EventHandler {

    private final static List<UUID> BLACKLIST = new ArrayList<>();
    private final static List<UUID> FROZEN = new ArrayList<>();
    private SyncDataService service;
    private DatabaseManager databaseManager;
    private SpongeExecutorService asyncExecutor;
    private boolean freezeInventory;
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
        freezeInventory = Invfly.instance.getConfigLoader().getConfig().general.freezeInventory;
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
        for (Player player:world.getPlayers()){
            if (!BLACKLIST.contains(player.getUniqueId())) {
                try {
                    service.saveUserData(player, false);
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                    Sponge.getServer()
                            .getConsole()
                            .sendMessage(Utils.toText(message.failedSaveDataWhenWorldSave).replace("%player%", Utils.toText(player.getName())));
                }
            }
        }
        asyncExecutor.submit(()-> databaseManager.deleteOutDate(outDate));
    }

    @Listener
    public void onConnection(ClientConnectionEvent.Auth event){
        UUID uuid = event.getProfile().getUniqueId();
        BLACKLIST.add(uuid);
        if (freezeInventory){
            FROZEN.add(uuid);
        }
    }

    @Listener
    public void loadPlayerData(LoadDataEvent.Done event){
        UUID uuid = event.getTargetUser().getUniqueId();
        BLACKLIST.remove(uuid);
        FROZEN.remove(uuid);
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event, @First Player player){
        UUID uuid = player.getUniqueId();
        asyncExecutor.scheduleAtFixedRate(new JoinProcess(uuid, player), delay, nextRetry * 1000, TimeUnit.MILLISECONDS);
    }

    @Listener
    public void disconnect(ClientConnectionEvent.Disconnect event, @First Player player){
        UUID uuid = player.getUniqueId();
        if (!BLACKLIST.contains(uuid)){
            asyncExecutor.submit(()->{
                try {
                    service.saveUserData(player, true);
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                    Text text = Utils.toText(message.failedSaveDataWhenDisconnect).replace("%player%", Utils.toText(player.getName()));
                    Sponge.getServer().getConsole().sendMessage(text);
                }
            });
        }
    }

    @Listener
    public void changeInv(ChangeInventoryEvent event, @First Player player){
        UUID uuid = player.getUniqueId();
        if (freezeInventory && FROZEN.contains(uuid)){
            event.setCancelled(true);
            player.sendMessage(Utils.toText(message.invLocked));
        }
    }

    private class JoinProcess extends TimerTask{

        private int count;
        private final UUID uuid;
        private final Player player;

        public JoinProcess(UUID uuid, Player player){
            this.count = 0;
            this.uuid = uuid;
            this.player = player;
        }

        @Override
        public void run() {
            if (BLACKLIST.contains(uuid) && (count < retry || retry == 0) ){
                StorageData data = databaseManager.getLatest(player);
                if (data == null){
                    player.sendMessage(Utils.toText(message.noData));
                    BLACKLIST.remove(uuid);
                    FROZEN.remove(uuid);
                }else {
                    if (data.isDisconnect()){
                        //right data
                        try {
                            service.loadUserData(player, data);
                            player.sendMessage(Utils.toText(message.loadSuccessful));
                        } catch (IllegalAccessException | InstantiationException e) {
                            e.printStackTrace();
                            player.sendMessage(Utils.toText(message.retry));
                        }
                    }else {
                        //wrong data
                        player.sendMessage(Utils.toText(message.retry).replace("%second%", Text.of(nextRetry)));
                    }
                }
            }else if (BLACKLIST.contains(uuid) && count == retry){
                if (!preventDirtyData){
                    try {
                        player.sendMessage(Utils.toText(message.retryFailAndTryToLoadLatestData));
                        service.loadUerData(player);
                        player.sendMessage(Utils.toText(message.loadSuccessful));
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        player.sendMessage(Utils.toText(message.finallyFailToLoad));
                    }
                }else {
                    player.sendMessage(Utils.toText(message.finallyFailToLoad));
                }
            }else if (count > retry){
                this.cancel();
            }
            count += 1;
        }
    }


}

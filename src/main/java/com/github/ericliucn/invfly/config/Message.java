package com.github.ericliucn.invfly.config;

import com.github.ericliucn.invfly.utils.Utils;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;


@ConfigSerializable
public class Message {

    @Setting
    public Gui gui;

    public Message() {
        gui = new Gui();
    }

    @ConfigSerializable
    public static class Gui{

        @Setting
        public String recoverToUser = " &d&n[User]";

        @Setting
        public String recoverToUserTip = "Click to recover this data to target user";

        @Setting
        public String recoverToMe = " &6&n[Me]";

        @Setting
        public String recoverToMeTip = "Click to recover this data to me";

        @Setting
        public String detail = " &a&n[Detail]";

        @Setting
        public String detailTip = "Click to show detail about this data";

        @Setting
        public String userName = "&bUser Name: &f";

        @Setting
        public String uuid = "&bUUID: &f";

        @Setting
        public String time = "&bTimestamp: &f";

        @Setting
        public String disconnect = "&bSaveWhenDisconnect: &f";

        @Setting
        public String server = "&bServer: &f";

        @Setting
        public String id = "&bID: &f";

        @Setting
        public String delete = " &n&4[Delete]";

        @Setting
        public String deleteTip = "Click to delete this record";

        @Setting
        public String title = "&6InvFly data of %user%";


    }

    @Setting
    public String noData = "&b&l[InvFly] &eNo data need to be sync, first join?";

    @Setting
    public String noValue = "&b&l[InvFly] &eNo value present for %data% ";

    @Setting
    public String retry = "&b&l[InvFly] &4Failed to load your data, retry in %second% seconds";

    @Setting
    public String finallyFailToLoad = "&b&l[InvFly] &4Failed to load your data, ask op for help please";

    @Setting
    public String retryFailAndTryToLoadLatestData = "&b&l[InvFly] &4All retries fail, try to load latest data";

    @Setting
    public String loadSuccessful = "&b&l[InvFly] &aSuccessful load data";

    @Setting
    public String loadFail = "&b&l[InvFly] &4Failed to load data";

    @Setting
    public String saveSuccessful = "&b&l[InvFly] &aSuccessful save data";

    @Setting
    public String saveFail = "&b&l[InvFly] &4Failed to save data";

    @Setting
    public String deleteSuccessful = "&b&l[InvFly] &aSuccessful delete data";

    @Setting
    public String deleteFail = "&b&l[InvFly] &4Failed to delete data";

    @Setting
    public String failedSaveDataWhenDisconnect = "&b&l[InvFly] &4Failed save data when %player% disconnect";

    @Setting
    public String invLocked = "&b&l[InvFly] &4Your inventory has been locked, please wait for the sync finish";

    @Setting
    public String failedSaveDataWhenWorldSave = "&b&l[InvFly] &4Failed save %player%'s data when world save";
    
    @Setting
    public String reloadSuccess = "&b&l[InvFly] &aReload Successful!";

    @Setting
    public String reloadFail = "&b&l[InvFly] &4Reload Failed!";
}

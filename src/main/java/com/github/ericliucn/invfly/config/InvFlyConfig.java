package com.github.ericliucn.invfly.config;


import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.time.Duration;

@ConfigSerializable
public class InvFlyConfig {

    @Setting
    public Storage storage;

    @Setting
    public General general;

    @Setting
    public Module module;

    public InvFlyConfig(){
        storage = new Storage();
        general = new General();
        module = new Module();
    }


    @ConfigSerializable
    public static class Storage {

        @Setting
        public Basic basic;

        @Setting(comment = "Don't touch this if you don't know what it is")
        public ConnectionPool connectionPool;

        public Storage(){
            basic = new Basic();
            connectionPool = new ConnectionPool();
        }

        @ConfigSerializable
        public static class Basic {

            @Setting(comment = "Storage method you want to use. Possible option: mysql/h2")
            public String method = "mysql";

            @Setting
            public String host = "127.0.0.1";

            @Setting
            public String tableName = "invfly";

            @Setting
            public int port = 3306;

            @Setting
            public String username = "username";

            @Setting
            public String password = "password";

            @Setting
            public String database = "database";

        }

        @ConfigSerializable
        public static class ConnectionPool {

            @Setting
            public int maxLifeTime = 1800000;

            @Setting
            public int maxPoolSize = 10;

            @Setting
            public int minIdleSize = 8;

            @Setting
            public int idleTimeout = 600000;

            @Setting
            public int connectionTimeout = 5000;
        }
    }

    @ConfigSerializable
    public static class General{

        @Setting(comment = "Data retention time (data in #d#h#m#s ago will be delete)")
        public Duration outDate = Duration.ofDays(1);

        @Setting(comment = "This server's name, for data tag")
        public String serverName = "GLOBAL";

        @Setting(comment = "Freeze player inventory before load data success")
        public boolean freezeInventory = true;

        @Setting(comment = "Due to the save data operation need some time \n " +
                "It's better to set a delay for load data \n " +
                "Especially for people who use bungeecord \n" +
                "When player switch server, it need some time to wait mysql finish the writing process \n" +
                "The unit is millisecond (1 second = 1000 milliseconds)")
        public long delay = 500;

        @Setting(comment = "If Invfly find the latest data does not marked as 'disconnect'\n" +
                "Which means the save data operation of previous server may not done yet\n" +
                "And if this value is greater than 0, Invfly will retry several times to wait for right data as you set")
        public int retry = 5;

        @Setting(comment = "Set this to true means if finally failed to load right data for player\n" +
                "This player's data will not be save or load in current server to prevent dirty data\n" +
                "And they are also not able to change any inventory\n" +
                "You can still to use commands to save/load, but that may cause problems\n" +
                "It that happen, you better let the player back to the previous server and check out what wrong\n" +
                "Set this to false means it will load the latest data for the player after failed to find appropriate data")
        public boolean preventDirtyData = false;

        @Setting(comment = "How long for next retry (seconds)")
        public int nextRetry = 1;

        @Setting
        public boolean saveWhenWorldSave = true;

    }

    @ConfigSerializable
    public static class Module{

        @Setting
        public boolean playerMainInventory = true;

        @Setting
        public boolean enderChestInventory = true;

        @Setting
        public boolean experience = true;

        @Setting
        public boolean gameMode = true;

        @Setting
        public boolean health = true;

        @Setting
        public boolean hotbarIndex = true;

        @Setting
        public boolean potion = true;

        @Setting
        public boolean food = true;

        @Setting(comment = "can fly/flying/fly speed")
        public boolean flyData = true;

    }

}


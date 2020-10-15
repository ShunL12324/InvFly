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
        public boolean lockInv = true;

        @Setting(comment = "Due to the save data operation need some time \n " +
                "It's better to set a delay for load data \n " +
                "Especially for people who use bungeecord \n" +
                "When player switch server, it need some time to wait mysql finish the writing process \n" +
                "The unit is millisecond (1 second = 1000 milliseconds)")
        public long initialDelayWhenJoin = 500;

        @Setting(comment = "If Invfly find the latest data does not marked as 'disconnect'\n" +
                "Which means the save data operation of previous server may not done yet\n" +
                "And if this value is greater than 0, Invfly will retry several times to wait for right data as you set")
        public int retryTimes = 5;

        @Setting(comment = "How long for next retry (seconds)")
        public int nextRetryTime = 1;

        @Setting(comment = "重试失败后是否加载能获取到的最新的数据")
        public boolean loadLatestWhenRetryFail = true;

        @Setting
        public int loadTimeOut = 5;

        @Setting(comment = "等待多少秒之后开始自动保存玩家数据定时任务，设置为 0 关闭")
        public int autoSaveDelay = 15;

        @Setting(comment = "每隔多少秒保存一次玩家数据， autoSaveDelay不为 0 时改设置有效")
        public int autoSaveInterval = 30;


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


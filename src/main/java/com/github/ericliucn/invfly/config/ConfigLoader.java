package com.github.ericliucn.invfly.config;

import com.github.ericliucn.invfly.config.configserializers.DurationSerializer;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class ConfigLoader {

    private InvFlyConfig config;
    private Message message;
    private final File root;
    private final File configFile;
    private final File messageFile;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private TypeSerializerCollection collection;

    public ConfigLoader(File root){
        this.root = root;
        this.configFile = new File(root, "invfly.conf");
        this.messageFile = new File(root, "message.properties");
        this.registerCustomSerializer();
        this.createDic(root);
        this.loadConfig();
        this.loadMessage();
    }

    private void registerCustomSerializer(){
        collection = TypeSerializers.getDefaultSerializers().newChild();
        collection.registerType(TypeToken.of(Duration.class), new DurationSerializer());
    }

    private void createDic(File file){
        if (!file.exists()){
            file.mkdir();
        }
    }


    private void loadConfig(){
        try {
            loader = HoconConfigurationLoader.builder().setFile(configFile).build();
            CommentedConfigurationNode configNode = loader.load(ConfigurationOptions.defaults().setSerializers(collection).setShouldCopyDefaults(true));
            this.config = configNode.getValue(TypeToken.of(InvFlyConfig.class), new InvFlyConfig());
            loader.save(configNode);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    private void loadMessage(){
        this.message = new Message(this.messageFile, this.root);
    }

    public InvFlyConfig getConfig() {
        return config;
    }

    public Message getMessage() {
        return message;
    }
}

package com.github.ericliucn.invfly.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class ConfigLoader {

    private InvFlyConfig config;
    private Message message;
    private final File configFile;
    private final File messageFile;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private TypeSerializerCollection collection;

    public ConfigLoader(File file){
        configFile = new File(file, "invfly.conf");
        messageFile = new File(file, "message.conf");
        this.registerCustomSerializer();
        this.createDic(file);
        this.loadConfig();
        this.loadMessage();
    }

    private void registerCustomSerializer(){
        collection = TypeSerializers.getDefaultSerializers().newChild();
        collection.registerType(TypeToken.of(Duration.class), new DurationSerializer());
        collection.registerType(TypeToken.of(Text.class), new TextsSerializer());
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
        try {
            loader = HoconConfigurationLoader.builder().setFile(messageFile).build();
            CommentedConfigurationNode messageNode = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            this.message = messageNode.getValue(TypeToken.of(Message.class), new Message());
            loader.save(messageNode);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public InvFlyConfig getConfig() {
        return config;
    }

    public Message getMessage() {
        return message;
    }
}

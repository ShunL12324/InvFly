package com.github.ericliucn.invfly.config;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.utils.Utils;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class Message {

    private final File messageFile;
    private final File root;
    private final Properties message;

    public Message(File messageFile, File root){
        this.message = new Properties();
        this.root = root;
        this.messageFile = messageFile;
        this.copyAsset();
        this.load();
    }

    private void copyAsset(){
        if (!this.messageFile.exists()){
            Invfly.instance.getPluginContainer().getAsset(messageFile.getName()).ifPresent(asset -> {
                try {
                    asset.copyToDirectory(root.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void load() {
        try {
            message.load(new InputStreamReader(new FileInputStream(this.messageFile), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Text getMessage(String key){
        return Utils.toText(this.message.getProperty(key));
    }
}

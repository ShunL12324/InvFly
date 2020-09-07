package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.data.GsonTypes;
import com.github.ericliucn.invfly.data.SyncData;
import com.google.gson.Gson;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PotionSyncData implements SyncData {

    public static Gson gson = new Gson();

    @Override
    public String getSerializedData(User user) {
        List<String> strings = new ArrayList<>();
        Optional<List<PotionEffect>> optionalPotionEffects = user.get(Keys.POTION_EFFECTS);
        if (optionalPotionEffects.isPresent()){
            List<PotionEffect> effects = optionalPotionEffects.get();
            for (PotionEffect effect:effects){
                try {
                    String singlePotion = DataFormats.JSON.write(effect.toContainer());
                    strings.add(singlePotion);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return gson.toJson(strings, GsonTypes.POTIONTYPE);
    }

    @Override
    public void deserialize(User user, String data) {
        List<String> strEffects = gson.fromJson(data, GsonTypes.POTIONTYPE);
        List<PotionEffect> potionEffects = new ArrayList<>();
        user.offer(Keys.POTION_EFFECTS, potionEffects);
        for (String s:strEffects){
            try {
                DataContainer container = DataFormats.JSON.read(s);
                PotionEffect.builder().build(container).ifPresent(potionEffects::add);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        user.offer(Keys.POTION_EFFECTS, potionEffects);
    }


    @Override
    public String getID() {
        return "potioneffects";
    }

    @Override
    public boolean shouldAsync() {
        return true;
    }


}

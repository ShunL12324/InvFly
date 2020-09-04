package com.github.ericliucn.invfly.data.datas;

import com.github.ericliucn.invfly.data.SyncData;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;

public class GameModeSyncData implements SyncData {

    @Override
    public String getSerializedData(User user) {
        GameMode gameMode = user.get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL);
        return gameMode.getId();
    }

    @Override
    public void deserialize(User user, String data) {
        switch (data){
            case "minecraft:creative":
                user.offer(Keys.GAME_MODE, GameModes.CREATIVE);
                break;
            case "minecraft:not_set":
                user.offer(Keys.GAME_MODE, GameModes.NOT_SET);
                break;
            case "minecraft:spectator":
                user.offer(Keys.GAME_MODE, GameModes.SPECTATOR);
                break;
            case "minecraft:adventure":
                user.offer(Keys.GAME_MODE, GameModes.ADVENTURE);
                break;
            default:
                user.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
                break;
        }
    }


    @Override
    public String getID() {
        return "gamemode";
    }
}

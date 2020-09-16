package com.github.ericliucn.invfly.utils;

import com.github.ericliucn.invfly.Invfly;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class Utils {

    public static Text toText(String s){
        return TextSerializers.FORMATTING_CODE.deserialize(s);
    }

    public static void postEvent(Event event){
        Invfly.instance.getSyncExecutor().submit(() -> Sponge.getEventManager().post(event));
    }

}

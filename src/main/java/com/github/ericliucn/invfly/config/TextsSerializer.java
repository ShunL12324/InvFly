package com.github.ericliucn.invfly.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class TextsSerializer implements TypeSerializer<Text> {

    @Override
    public @Nullable Text deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        String string = value.getString();
        return TextSerializers.FORMATTING_CODE.deserialize(string != null ? string : "null");
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable Text obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj != null) {
            value.setValue(TextSerializers.FORMATTING_CODE.serialize(obj));
        }
    }

}

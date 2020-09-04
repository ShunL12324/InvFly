package invfly.utils;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class Utils {

    public static Text toText(String s){
        return TextSerializers.FORMATTING_CODE.deserialize(s);
    }
}

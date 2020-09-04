package invfly.data;

import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GsonTypes {

    public static final Type INVTYPE = new TypeToken<ArrayList<Pair<Integer, String>>>(){}.getType();

    public static final Type ALLDATATYPE = new TypeToken<HashMap<String, String>>(){}.getType();

    public static final Type POTIONTYPE = new TypeToken<List<String>>(){}.getType();

    public static final Type HEAL = new TypeToken<Map<String, Double>>(){}.getType();

}

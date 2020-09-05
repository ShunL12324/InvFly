package com.github.ericliucn.invfly.data;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class GsonTypes {

    public static final Type INVTYPE = new TypeToken<HashMap<Integer, String>>(){}.getType();

    public static final Type ALLDATATYPE = new TypeToken<HashMap<String, String>>(){}.getType();

    public static final Type POTIONTYPE = new TypeToken<ArrayList<String>>(){}.getType();

    public static final Type HEAL = new TypeToken<HashMap<String, Double>>(){}.getType();

}

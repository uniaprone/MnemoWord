package com.kite.mnemoai.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.kite.mnemoai.database.model.WordExtract;

public class Converters {
    private static final Gson gson = new Gson();
    @TypeConverter
    public static WordExtract stringToWordExtract(String extractJson) {
        return gson.fromJson(extractJson, WordExtract.class);
    }

    @TypeConverter
    public static String wordExtractToSting(WordExtract wordExtract){
        return gson.toJson(wordExtract);
    }

}

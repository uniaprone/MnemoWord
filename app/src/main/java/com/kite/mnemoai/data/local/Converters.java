package com.kite.mnemoai.data.local;

import android.os.Build;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kite.mnemoai.data.model.WordExtract;

import java.time.LocalDate;

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

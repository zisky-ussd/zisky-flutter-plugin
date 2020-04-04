package com.zisky.zisky;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.setDateFormat(new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSSZ"));
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
    public static String getAssetJsonData(Context context,String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        Log.d("APP-2020", json);//todo remove when publishing to maven
        return json;

    }

    public static String toJson(Object object)  {

        try {
            return getJacksonObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LogUtil.w("Error occurred during jackson serialize", e);
            return null;
        }
    }

    public static ObjectMapper getJacksonObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.setDateFormat(DateUtil.DATE_FORMAT);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public static <T> T clone(T object, Class<T> type) {
        return fromJson(toJson(object), type);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            if (json == null) {
                return null;
            }

            return getJacksonObjectMapper().readValue(json, type);
        } catch (IOException e) {
            LogUtil.w("Error occurred during jackson deserialize for type: " + type.getName(), e);
            return null;
        }
    }

    public static <T> T fromMap(LinkedHashMap map, Class<T> type) {
        return fromJson(toJson(map), type);
    }
}

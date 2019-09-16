package com.eb.services.impl;

import com.eb.services.Preprocessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.eb.model.LoggerEvent;

public enum JsonEventDeserializer implements Preprocessor {
    INSTANCE;

    private Gson gson = new GsonBuilder().create();

    @Override
    public LoggerEvent deserialize(String line) {
        return gson.fromJson(line, LoggerEvent.class);
    }
}

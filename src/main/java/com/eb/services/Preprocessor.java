package com.eb.services;

import com.eb.model.LoggerEvent;

public interface Preprocessor {
    LoggerEvent deserialize(String line);
}

package com.eb.processor;

import com.eb.model.LoggerEvent;

public interface Processor {
    void process(LoggerEvent event);

    void shutdown();
}

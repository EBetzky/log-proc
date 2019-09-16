package com.eb.services;

import com.eb.model.ProcessedEventEntity;

public interface Output {
    void write(ProcessedEventEntity entity);

    void shutdown();
}

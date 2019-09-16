package com.eb.services.impl;

import com.eb.services.Input;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static java.nio.file.Files.lines;

public enum EventFileReader implements Input {
    INSTANCE;

    @Override
    public Stream<String> read(final String filename) throws IOException {
        return lines(new File(filename).toPath());
    }
}

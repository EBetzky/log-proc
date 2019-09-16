package com.eb.services;

import java.io.IOException;
import java.util.stream.Stream;

public interface Input {
    Stream<String> read(final String filename) throws IOException;
}

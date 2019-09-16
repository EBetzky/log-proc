package com.eb;

import com.eb.processor.impl.ParallelEventProcessor;
import com.eb.processor.Processor;
import com.eb.services.impl.EventFileReader;
import com.eb.services.impl.HsqldbProcessedEventPersistence;
import com.eb.services.Input;
import com.eb.services.impl.JsonEventDeserializer;
import com.eb.services.Output;
import com.eb.services.Preprocessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static java.lang.System.exit;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Welcome. This is a tool for processing logger events.");

        final String pathToFile = args.length > 0 ? args[0] : null;
        if (null == pathToFile) {
            LOGGER.fatal("No file containing events for processing specified (1st and only arg). Exiting.");
            exit(1);
        }

        // dependencies
        Input input = EventFileReader.INSTANCE;
        Preprocessor preprocessor = JsonEventDeserializer.INSTANCE;
        Output output = new HsqldbProcessedEventPersistence();
        Processor processor = new ParallelEventProcessor(output);

        try {
            input.read(pathToFile).
                    parallel().
                    map(preprocessor::deserialize).
                    forEach(processor::process);
        } catch (IOException e) {
            LOGGER.fatal("There was an I/O problem during reading the log file to be processed. Program will exit.", e);
        } finally {
            processor.shutdown();
        }
    }
}

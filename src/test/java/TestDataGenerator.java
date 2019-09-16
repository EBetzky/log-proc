import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;


// NOT TO BE ASSESSED: test data generator
public class TestDataGenerator {

    private static final int SIZE = 10000;

    @Ignore
    @Test
    public void create() throws IOException {
        List<String> lines = new ArrayList<>();

        Random random = new Random();

        final String started ="{\"id\":\"%s\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":%d}\n";
        final String finished ="{\"id\":\"%s\", \"state\":\"FINISHED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":%d}\n";

        for (int i = 0; i < SIZE; i++) {
            UUID id = UUID.randomUUID();
            long timestamp = System.currentTimeMillis();
            //int counter
            String s = String.format(started, id, timestamp);
            String f = String.format(finished, id, timestamp + random.nextInt(100));

            lines.add(s);
            lines.add(f);
        }

        Collections.shuffle(lines);

        BufferedWriter writer = new BufferedWriter(new FileWriter("test.json", true));
        for (String s : lines) {
            writer.write(s);
        }
        writer.close();
    }
}

package com.eb.processor.impl;

import com.eb.model.LoggerEvent;
import com.eb.model.ProcessedEventEntity;
import com.eb.model.State;
import com.eb.services.Output;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ParallelEventProcessorTest {

    @Mock
    private Output output;

    private ParallelEventProcessor processor;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private LoggerEvent started;
    private LoggerEvent finished;

    @Before
    public void setup() {
        processor = new ParallelEventProcessor(output);
    }

    @Test
    public void shouldNotFindPairWhenOneEvent() throws ExecutionException, InterruptedException {
        // given
        setupStarted();

        // when
        Map<State, LoggerEvent> result = callFindPair(started);

        // then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void shouldFindPairWhenTwoEvents() throws ExecutionException, InterruptedException {
        // given
        setupStarted();
        setupFinished();

        // when
        Map<State, LoggerEvent> result = callFindPair(started);
        Map<State, LoggerEvent> finalResult = callFindPair(finished);

        // then
        assertEmptyMap(result);

        assertNotNull(finalResult);
        assertEquals(2, finalResult.size());
        assertEquals(started, finalResult.get(State.STARTED));
        assertEquals(finished, finalResult.get(State.FINISHED));
    }

    @Test
    public void shouldFindPairWhenThereIsEmptyId() throws ExecutionException, InterruptedException {
        // given
        started = new LoggerEvent(null, State.STARTED, "type", "host", 10L);
        setupFinished();

        // when
        Map<State, LoggerEvent> result = callFindPair(started);
        Map<State, LoggerEvent> finalResult = callFindPair(finished);

        // then
        assertEmptyMap(result);
        assertEmptyMap(finalResult);
    }

    @Test
    public void shouldFindPairWhenThereAreSameStates() throws ExecutionException, InterruptedException {
        // given
        started = new LoggerEvent("id", State.FINISHED, "type", "host", 10L);
        setupFinished();

        // when
        Map<State, LoggerEvent> result = callFindPair(started);
        Map<State, LoggerEvent> finalResult = callFindPair(finished);

        // then
        assertEmptyMap(result);
        assertEmptyMap(finalResult);
    }

    private void assertEmptyMap(Map<State, LoggerEvent> result) {
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    private Map<State, LoggerEvent> callFindPair(LoggerEvent event) throws ExecutionException, InterruptedException {
        Callable<Map<State, LoggerEvent>> findPair = processor.findPair(event);
        Future<Map<State, LoggerEvent>> findPairResult = executor.submit(findPair);
        return findPairResult.get();
    }

    private void setupStarted() {
        started = new LoggerEvent("id", State.STARTED, "type", "host", 2L);
    }

    private void setupFinished() {
        finished = new LoggerEvent("id", State.FINISHED, "type", "host", 4L);
    }

    //TODO: remove duplication
    @Test
    public void calculateDuration() throws ExecutionException, InterruptedException {
        // given
        setupStarted();
        setupFinished();
        Map<State, LoggerEvent> pair = new EnumMap<>(State.class);
        pair.put(State.STARTED, started);
        pair.put(State.FINISHED, finished);

        // when
        Callable<ProcessedEventEntity> calculateDuration = processor.calculateDuration(pair);
        Future<ProcessedEventEntity> durationCalculationResult = executor.submit(calculateDuration);
        ProcessedEventEntity processedEvent = durationCalculationResult.get();

        // then
        assertNotNull(processedEvent);
        assertEquals("id", processedEvent.getId());
        assertEquals(2L, processedEvent.getDuration());
        assertEquals("type", processedEvent.getType());
        assertEquals("host", processedEvent.getHost());
        assertFalse(processedEvent.isAlert());
    }

    @Test
    public void calculateDurationWhenAlerted() throws ExecutionException, InterruptedException {
        // given
        setupStarted();
        finished = new LoggerEvent("id", State.FINISHED, "type", "host", 8L);
        Map<State, LoggerEvent> pair = new EnumMap<>(State.class);
        pair.put(State.STARTED, started);
        pair.put(State.FINISHED, finished);

        // when
        Callable<ProcessedEventEntity> calculateDuration = processor.calculateDuration(pair);
        Future<ProcessedEventEntity> durationCalculationResult = executor.submit(calculateDuration);
        ProcessedEventEntity processedEvent = durationCalculationResult.get();

        // then
        assertNotNull(processedEvent);
        assertEquals("id", processedEvent.getId());
        assertEquals(6, processedEvent.getDuration());
        assertEquals("type", processedEvent.getType());
        assertEquals("host", processedEvent.getHost());
        assertTrue(processedEvent.isAlert());
    }

    @Test
    public void process() {
        //TODO:
    }

    @Test
    public void persistFinalEvent() throws ExecutionException, InterruptedException {
        //TODO:

    }

}
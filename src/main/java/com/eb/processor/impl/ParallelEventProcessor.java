package com.eb.processor.impl;

import com.eb.model.ProcessedEventEntity;
import com.eb.model.LoggerEvent;
import com.eb.model.State;
import com.eb.processor.Processor;
import com.eb.services.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.Collections.EMPTY_MAP;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static com.eb.model.State.FINISHED;
import static com.eb.model.State.STARTED;

public class ParallelEventProcessor implements Processor {

    private static final int PROCESSING_QUEUE_CAPACITY = 10000;
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAXIMUM_POOL_SIZE = 8;
    private static final Logger LOGGER = LogManager.getLogger(ParallelEventProcessor.class);

    private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(PROCESSING_QUEUE_CAPACITY);
    private ExecutorService executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 0L, MILLISECONDS, queue);
    private ConcurrentMap<String, LoggerEvent> events = new ConcurrentHashMap<>();

    private Output outputService;
    private Integer pairsFound = 0;

    public ParallelEventProcessor(Output outputService) {
        this.outputService = outputService;
    }

    @Override
    public void process(LoggerEvent event) {
        try {
            Future<Map<State, LoggerEvent>> findPairResult = executor.submit(findPair(event));
            Map<State, LoggerEvent> pair = findPairResult.get();
            if (pair.isEmpty()) {
                return;
            }
            Future<ProcessedEventEntity> durationCalculationResult = executor.submit(calculateDuration(pair));
            ProcessedEventEntity processedEvent = durationCalculationResult.get();

            Future<Void> eventPersistenceResult = executor.submit(persistFinalEvent(processedEvent));
            eventPersistenceResult.get();

        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Problem during processing event: {}. Root cause: {}", event.getId(), e);
        }
    }

    Callable<Map<State, LoggerEvent>> findPair(final LoggerEvent event) {
        return () -> {
            Map<State, LoggerEvent> result = EMPTY_MAP;
            final String id = event.getId();

            if (null == id) {
                LOGGER.warn("Event does not have id. Skipping processing for this event:\n{}", event);
            } else {
                final State state = event.getState();

                synchronized (this.events) {
                    LoggerEvent fetchedEvent = events.remove(event.getId());
                    if (null != fetchedEvent) {
                        if (state != fetchedEvent.getState()) {
                            result = createStateLoggerEventMap(event, fetchedEvent);
                        } else {
                            LOGGER.warn(
                                    "Event pair STARTED-FINISHED not associated - skipping processing. Event with duplicated state {} for event id: {}", state, id);
                        }
                    } else {
                        LOGGER.trace("No match found. Putting into cache event: {}", id);
                        events.put(id, event);
                    }
                }
            }
            return result;
        };
    }

    private Map<State, LoggerEvent> createStateLoggerEventMap(LoggerEvent first, LoggerEvent second) {
        Map<State, LoggerEvent> result;
        LOGGER.debug("Event pair no. {} has been found, id: {}", ++pairsFound, first.getId());
        result = new EnumMap<>(State.class);
        result.put(first.getState(), first);
        result.put(second.getState(), second);
        return result;
    }

    Callable<ProcessedEventEntity> calculateDuration(final Map<State, LoggerEvent> pair) {
        return () -> {
            final int allowedDurationTime = 4;

            LoggerEvent started = pair.get(STARTED);
            LoggerEvent finished = pair.get(FINISHED);

            int duration = (int) (finished.getTimestamp() - started.getTimestamp());
            String type = null != started.getType() ? started.getType() : finished.getType();
            String host = null != started.getHost() ? started.getHost() : finished.getHost();
            boolean alert = duration > allowedDurationTime;

            return new ProcessedEventEntity(started.getId(), duration, type, host, alert);
        };
    }

    Callable<Void> persistFinalEvent(ProcessedEventEntity entity) {
        return () -> {
            outputService.write(entity);
            return null;
        };
    }

    @Override
    public void shutdown() {
        try {
            while (queue.size() > 0) {
                SECONDS.sleep(2);
            }
            outputService.shutdown();   //?
            executor.shutdown();
            LOGGER.info("Shutting down workers. Overall STARTED-FINISHED event pairs processed: {}", pairsFound);

            if (!executor.awaitTermination(1, SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.error("There was problem during shutting down the log processor.", e);
            executor.shutdownNow(); // try again?
        }
        LOGGER.info("Work is done. BYe bye.");
    }

}

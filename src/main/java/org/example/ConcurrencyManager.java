package org.example;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ConcurrencyManager implements AutoCloseable {
    private final Set<URI> foundURLs;
    private final ExecutorService executor;
    private final Lock lock;
    private final Phaser phaser;

    public ConcurrencyManager() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.foundURLs = new HashSet<>();
        this.lock = new ReentrantReadWriteLock().writeLock();
        this.phaser = new Phaser();
    }

    public Set<URI> scrape (URI rootUrl) {
        phaser.register();
        scheduleScraping(Set.of(rootUrl));
        phaser.arriveAndAwaitAdvance();
        return foundURLs;
    }

    private void decrementPhaser() {
        phaser.arriveAndDeregister();
    }

    private void scheduleScraping(Set<URI> potentialURLs) {
        lock.lock();
        Set<URI> newURLs;

        try {
            newURLs = potentialURLs
                    .stream()
                    .filter(e -> !foundURLs.contains(e))
                    .collect(Collectors.toSet());
            foundURLs.addAll(newURLs);
        } finally {
            lock.unlock();
        }

        for (URI newUrl : newURLs) {
            System.err.println("Requesting scrape of " + newUrl);
            try {
                phaser.register();
                executor.submit(
                        new ScrapeRequestHandler(newUrl, this::scheduleScraping, this::decrementPhaser)
                );
            } catch (NullPointerException | RejectedExecutionException e) {
                System.err.println(e.getMessage());
                decrementPhaser();
            }
        }
    }

    @Override
    public void close() {
        executor.close();
    }
}

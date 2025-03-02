package org.example;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ConcurrencyManager implements AutoCloseable {
    private final Set<String> foundURLs;
    private final ExecutorService executor;
    private final Lock lock;
    private final Phaser phaser;

    public ConcurrencyManager() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.foundURLs = new HashSet<>();
        this.lock = new ReentrantReadWriteLock().writeLock();
        this.phaser = new Phaser();
    }

    public Set<String> scrape (String rootUrl) {
        phaser.register();
        scheduleScraping(Set.of(rootUrl));
        phaser.arriveAndAwaitAdvance();
        return foundURLs;
    }

    private void decrementPhaser() {
        phaser.arriveAndDeregister();
    }

    private void scheduleScraping(Set<String> potentialURLs) {
        lock.lock();
        try {
            Set<String> newURLs = potentialURLs
                    .stream()
                    .filter(e -> !foundURLs.contains(e))
                    .collect(Collectors.toSet());
            foundURLs.addAll(newURLs);
            for (String newUrl : newURLs) {
                System.out.println("Requesting scrape of " + newUrl);
                phaser.register(); // TODO: What if ScrapeRequestHandler throws on init?
                executor.submit(new ScrapeRequestHandler(newUrl, this::scheduleScraping, this::decrementPhaser));
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        executor.close();
    }
}

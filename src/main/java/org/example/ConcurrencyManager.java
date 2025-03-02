package org.example;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ConcurrencyManager {
    private final Set<String> foundURLs;
    private final ExecutorService executor;
    private final Lock lock;

    public ConcurrencyManager(ExecutorService executor) {
        this.executor = executor;
        this.foundURLs = new HashSet<>();
        this.lock = new ReentrantReadWriteLock().writeLock();
    }

    public void scheduleScraping(Set<String> potentialURLs) {
        lock.lock();
        try {
            Set<String> newURLs = potentialURLs
                    .stream()
                    .filter(e -> !foundURLs.contains(e))
                    .collect(Collectors.toSet());
            foundURLs.addAll(newURLs);
            for (String newUrl : newURLs) {
                System.out.println("Requesting scrape of " + newUrl);
                executor.submit(new ScrapeRequestHandler(newUrl, this::scheduleScraping));
            }
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getFoundURLs() {
        return foundURLs;
    }
}

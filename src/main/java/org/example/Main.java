package org.example;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Please provide the initial URL!");
        }
        String rootURL = args[0];

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()){
            ConcurrencyManager concMgr = new ConcurrencyManager(executor);
            concMgr.scheduleScraping(Set.of(rootURL));
            boolean exitedByItself = executor.awaitTermination(1, TimeUnit.MINUTES);
            System.out.println("exitedByItself = " + exitedByItself);
            System.out.println(concMgr.getFoundURLs());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
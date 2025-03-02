package org.example;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Please provide the initial URL!");
        }
        String rootURL = args[0];

        try (ConcurrencyManager concMgr = new ConcurrencyManager()){
            Set<String> foundUrls = concMgr.scrape(rootURL);
            System.out.println(foundUrls);
        }
    }
}
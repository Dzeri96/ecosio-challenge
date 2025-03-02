package org.example;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Please provide the initial URL!");
        }

        URI rootURL = new URI(args[0]);
        try (ConcurrencyManager concMgr = new ConcurrencyManager()){
            Set<URI> foundUrls = concMgr.scrape(rootURL);
            System.out.println(String.join("\n", foundUrls.stream().map(Object::toString).collect(Collectors.toSet())));
        }
    }
}
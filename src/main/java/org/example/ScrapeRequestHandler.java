package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScrapeRequestHandler implements Runnable {
    private final String url;
    private final Consumer<Set<String>> resultCallback;
    private final HttpClient httpClient;
    private final Pattern hrefPattern = Pattern.compile("a href=['\"]([^'\"]+)['\"]");

    public ScrapeRequestHandler(String url, Consumer<Set<String>> resultCallback) {
        this.url = url;
        this.resultCallback = resultCallback;
        this.httpClient = HttpClient
                .newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    @Override
    public void run() {
        try {
            URI rootURI = new URI(url);
            String rootHost = rootURI.getHost();

            HttpRequest request = HttpRequest
                    .newBuilder(rootURI)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Set<String> foundURLs = new HashSet<>();
                Matcher hrefMatcher = hrefPattern.matcher(response.body());
                while (hrefMatcher.find()) {
                    String rawHref = hrefMatcher.group(1);
                    URI parsedHref = new URI(rawHref);

                    if (parsedHref.getHost() != null && parsedHref.getHost().equals(rootHost)) {
                        foundURLs.add(rawHref);
                    } else if (parsedHref.getHost() == null) {
                        URI absoluteHrefURI = new URI(
                                rootURI.getScheme(),
                                rootURI.getHost(),
                                parsedHref.getPath(),
                                parsedHref.getQuery(),
                                parsedHref.getFragment()
                        );
                        foundURLs.add(absoluteHrefURI.toString());
                    }
                }

                //System.out.println("response = " + response.body());
                //System.out.println("foundURLs = " + foundURLs);
                resultCallback.accept(foundURLs);
            } else {
                System.err.println("Request to " + url + " returned code " + response.statusCode());
            }
            

        } catch (URISyntaxException e) {
            System.err.println("URISyntaxException: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.err.println("Request to " + url + " interrupted!");
        }
    }
}

package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScrapeRequestHandler implements Runnable {
    private final URI rootURI;
    private final Consumer<Set<URI>> resultCallback;
    private final Runnable finishCallback;
    private final HttpClient httpClient;
    private final Pattern hrefPattern = Pattern.compile("a href=['\"](?=/|http)(((?!\\.png|\\.jpg|\\.jpeg|\\.svg|\\.pdf)[^'\"])+)['\"]");

    public ScrapeRequestHandler(
            URI url,
            Consumer<Set<URI>> resultCallback,
            Runnable finishCallback
    ) {
        this.rootURI = url;
        this.resultCallback = resultCallback;
        this.finishCallback = finishCallback;
        this.httpClient = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    private void handleResponse(HttpResponse<String> response) {
        String rootHost = rootURI.getHost();

        if (response.statusCode() == 200) {
            Set<URI> foundURLs = new HashSet<>();
            Matcher hrefMatcher = hrefPattern.matcher(response.body());
            while (hrefMatcher.find()) {
                String rawHref = hrefMatcher.group(1);
                try {
                    URI parsedHref = new URI(rawHref);

                    if (parsedHref.getHost() != null && parsedHref.getHost().equals(rootHost)) {
                        foundURLs.add(parsedHref);
                    } else if (parsedHref.getHost() == null) {
                        URI absoluteHrefURI = new URI(
                                rootURI.getScheme(),
                                rootURI.getHost(),
                                parsedHref.getPath(),
                                parsedHref.getQuery(),
                                parsedHref.getFragment()
                        );
                        foundURLs.add(absoluteHrefURI);
                    }
                } catch (URISyntaxException e) {
                    System.err.println("URISyntaxException: " + e.getMessage());
                }
            }

            resultCallback.accept(foundURLs);
        } else {
            System.err.println("Request to " + rootURI + " returned code " + response.statusCode());
        }
    }

    @Override
    public void run() {
        try {
            HttpRequest request = HttpRequest
                    .newBuilder(rootURI)
                    .header("User-Agent", "curl/8.5.0")
                    .header("Accept", "text/html")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofString());

            handleResponse(response);

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.err.println("Request to " + rootURI + " interrupted!");
        } finally {
            System.err.println("Finished request to " + rootURI);
            finishCallback.run();
        }
    }
}

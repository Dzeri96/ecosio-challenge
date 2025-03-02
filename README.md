# Ecosio Challenge

For this challenge I decided to try out virtual threads for the first time.
This led me away from the traditional producer-consumer pattern with a fixed number of consumers,
and sort of forced me to use some more niche concurrency APIs like `Phaser`.
Still, I consider the solution interesting, so I decided to keep it.


One thing that needs to be looked into more is definitely the `HttpClient`.
It _should_ cause a virtual thread to block, but it also asks for its own executor.
Ideally, we would have one instance of the client with a reference to the virtual thread executor,
but without a semaphore,
it hits a limit of max. concurrent connections with the server.
I came across [this article](https://medium.com/@phil_3582/java-virtual-threads-some-early-gotchas-to-look-out-for-f65df1bad0db)
quite late in the dev process,
but it's quite interesting and shows not everything is straightforward in the land of virtual threads.
Overall, I'm pretty sure that the current setup does not utilize the full IO potential of the machine.

As a scraper, the solution is not perfect.
It relies on a hand-crafted regex pattern and does not handle very slow responses.
This can be seen on the ecosio website from time to time.
I didn't want to spend too much time with these problems as the scraper always terminates anyway.

## Things not considered
- Rate limiting
- Interrupts and signals
- Semantic equivalence of URLs, e.g. (`orf.at/` vs. `orf.at/#`)
- Non-href links
- Domain vs. subdomain check. Only the `host` part of a Java `URI` was checked for equivalence

## Running the code
The main method takes the URL to scrape as the first argument.

Please run the code with IntelliJ or build the Jar yourself or execute `mvn exec:java -Dexec.args="WEBSITE"`.
The debug information goes out on STDERR so you can just pipe the output.
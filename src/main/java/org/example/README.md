# Ecosio Challenge

For this challenge I decided to try out virtual threads for the first time.
This led me away from the traditional producer-consumer pattern with a fixed number of consumers,
and sort of forced me to use some more niche concurrency APIs like `Phaser`.


## Things not considered
- Rate limiting
- Interrupts and signals
- Semantic equivalence of URLs, e.g. (`orf.at/` vs. `orf.at/#`)
- Non-href links
- Domain vs. subdomain check. Only the `host` part of a Java `URI` was checked for equivalence
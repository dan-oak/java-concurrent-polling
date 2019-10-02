package dev.danoak

import dev.danoak.jobs.RandomIntegerPersister
import dev.danoak.pollers.Poller
import dev.danoak.repositories.ListRepository
import dev.danoak.repositories.Repository

import static java.util.concurrent.TimeUnit.SECONDS

class PollerTest extends Test {

    Poller poller = new Poller()

    def pollForDivisibleBy4(Repository<Number> repo) throws InterruptedException {
        def pollee = {
            repo.findFirst({ isDivisibleBy(it, 4) })
        }
        return poller.poll(pollee, 1, SECONDS, 5, SECONDS)
    }

    def "poller"() {
        given:
            def repo = new ListRepository<Integer>()
            def rng = new RandomIntegerPersister(repo)
            rng.start(1, 1, SECONDS)

        expect:
            pollForDivisibleBy4(repo)
                .map({ log.info("Found: {}", it); it })
                .isPresent()

        cleanup:
            rng.stop()
            log.info("Repo: {}", repo)
    }

}

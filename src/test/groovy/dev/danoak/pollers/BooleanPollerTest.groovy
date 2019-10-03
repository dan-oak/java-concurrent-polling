package dev.danoak.pollers

import dev.danoak.Test
import dev.danoak.jobs.RandomIntegerPersister
import dev.danoak.repositories.ListRepository
import dev.danoak.repositories.Repository

import static java.util.concurrent.TimeUnit.SECONDS

class BooleanPollerTest extends Test {

    def poller = new BooleanPoller()

    def pollForDivisibleBy4IsPresent(Repository<Number> repo) throws InterruptedException {
        def pollee = {
            repo.findFirst({ isDivisibleBy(it, 4) })
                .map({ log.info("Found: {}", it); it })
                .isPresent()
        }
        return poller.poll(pollee, 1, SECONDS, 5, SECONDS)
    }

    def "boolean poller"() {
        given:
            def repo = new ListRepository<Integer>()
            def rng = new RandomIntegerPersister(repo)
            rng.start(1, 1, SECONDS)

        expect:
            pollForDivisibleBy4IsPresent(repo)

        cleanup:
            rng.stop()
            log.info("Repo: {}", repo)
    }

}

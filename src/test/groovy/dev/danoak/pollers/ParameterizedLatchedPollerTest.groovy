package dev.danoak.pollers

import dev.danoak.Test
import dev.danoak.jobs.RandomIntegerPersister
import dev.danoak.repositories.ListRepository
import dev.danoak.repositories.Repository

import static java.util.concurrent.TimeUnit.SECONDS

class ParameterizedLatchedPollerTest extends Test {

    def poller = new ParameterizedLatchedPoller()

    def pollForDivisibleBy4(Repository<Number> repo) throws InterruptedException {
        def pollee = {
            repo.findFirst({ isDivisibleBy(it, 4) })
        }
        return poller.poll(pollee, 1, SECONDS, 5, SECONDS)
    }

    def "parameterized latched poller"() {
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

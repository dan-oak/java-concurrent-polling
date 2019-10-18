package dev.danoak.pollers

import dev.danoak.Test
import dev.danoak.jobs.RandomIntegerPersister
import dev.danoak.repositories.ListRepository
import dev.danoak.repositories.Repository

import static java.util.concurrent.TimeUnit.SECONDS

class ThreadPollerTest extends Test {

    def poller = new ThreadPoller()

    def <T> Optional<T> pollForIsDivisibleBy(Repository<T> repo, T n) throws InterruptedException {
        def pollee = {
            repo.findFirst({ isDivisibleBy(it, n) })
        }
        return poller.poll(pollee, 1, SECONDS, 5, SECONDS)
    }

    def "thead poller"() {
        given:
            def repo = new ListRepository<Integer>()
            def rng = new RandomIntegerPersister(repo)
            rng.start(1, 1, SECONDS)

        expect:
            pollForIsDivisibleBy(repo, 4)
                .map({ log.info("Found: {}", it); it })
                .isPresent()

        cleanup:
            rng.stop()
            log.info("Repo: {}", repo)
    }

}

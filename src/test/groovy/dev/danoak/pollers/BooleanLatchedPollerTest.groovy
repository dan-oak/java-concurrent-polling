package dev.danoak.pollers

import dev.danoak.Test
import dev.danoak.jobs.RandomIntegerPersister
import dev.danoak.repositories.ListRepository
import dev.danoak.repositories.Repository

import static java.util.concurrent.TimeUnit.SECONDS

class BooleanLatchedPollerTest extends Test {

    def poller = new BooleanLatchedPoller()

    def <T> Boolean pollForIsDivisibleByIsPresent(Repository<Number> repo, T n) throws InterruptedException {
        def pollee = {
            repo.findFirst({ isDivisibleBy(it, n) })
                .map({ log.info("Found: {}", it); it })
                .isPresent()
        }
        return poller.poll(pollee, 1, SECONDS).await(5, SECONDS)
    }

    def "boolean latched poller"() {
        given:
            def repo = new ListRepository<Integer>()
            def rng = new RandomIntegerPersister(repo)
            rng.start(1, 1, SECONDS)

        expect:
            pollForIsDivisibleByIsPresent(repo, 4)

        cleanup:
            rng.stop()
            log.info("Repo: {}", repo)
    }

}

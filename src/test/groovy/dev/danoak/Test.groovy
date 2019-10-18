package dev.danoak

import org.slf4j.LoggerFactory
import spock.lang.Specification

abstract class Test extends Specification {

    def log = LoggerFactory.getLogger(this.getClass().getName())

    def <T> Boolean isDivisibleBy(T n, T d) {
        return n % d == 0
    }

}
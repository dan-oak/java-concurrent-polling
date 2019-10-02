package dev.danoak

import org.slf4j.LoggerFactory
import spock.lang.Specification

abstract class Test extends Specification {

    def log = LoggerFactory.getLogger(this.getClass().getName())

    def isDivisibleBy(Number n, Number d) {
        return n % d == 0
    }

}
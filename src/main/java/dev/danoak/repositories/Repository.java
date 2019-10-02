package dev.danoak.repositories;

import java.util.Optional;
import java.util.function.Predicate;

public interface Repository<T> {
    Optional<T> findFirst(Predicate<T> p);
    Optional<T> save(T o);
}

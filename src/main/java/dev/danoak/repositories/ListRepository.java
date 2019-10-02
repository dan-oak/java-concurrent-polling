package dev.danoak.repositories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

public class ListRepository<T> implements Repository<T> {
    private final ArrayList<T> l = new ArrayList<>();
    public Optional<T> findFirst(Predicate<T> p) { return l.stream().filter(p).findFirst(); }
    public Optional<T> save(T o) { return Optional.ofNullable(l.add(o) ? o : null); }
    public String toString() { return l.toString(); }
}

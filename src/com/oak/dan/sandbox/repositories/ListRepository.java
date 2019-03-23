package com.oak.dan.sandbox.repositories;

import java.util.*;
import java.util.function.*;

public class ListRepository<T> implements Repository<T> {
    private final ArrayList<T> l = new ArrayList<>();
    public Optional<T> findFirst(Predicate<T> p) { return l.stream().filter(p).findFirst(); }
    public Optional<T> save(T o) { return Optional.ofNullable(l.add(o) ? o : null); }
}

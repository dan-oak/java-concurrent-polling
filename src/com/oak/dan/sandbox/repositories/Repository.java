package com.oak.dan.sandbox.repositories;

import java.util.*;
import java.util.function.*;

public interface Repository<T> {
    Optional<T> findFirst(Predicate<T> p);
    Optional<T> save(T o);
}

package com.sprint.mission.discodeit.repository.jcf;

import java.util.function.Predicate;

@FunctionalInterface
public interface JCFSelectFilter<T> {
    boolean filter(T c);
}

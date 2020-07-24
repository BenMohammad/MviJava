package com.benmohammad.mvijava.util;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Pair<F, S> {

    abstract  public F First();
    abstract public S Second();

    public static<A, B> Pair<A, B> create(A a, B b) {
        return new AutoValue_Pair<>(a, b);
    }
}

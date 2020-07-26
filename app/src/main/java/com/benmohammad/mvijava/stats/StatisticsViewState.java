package com.benmohammad.mvijava.stats;

import androidx.annotation.Nullable;

import com.benmohammad.mvijava.mvibase.MviViewState;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StatisticsViewState implements MviViewState {

    abstract boolean isLoading();
    abstract int activeCount();
    abstract int completedCount();
    @Nullable
    abstract Throwable error();

    static StatisticsViewState idle() {
        return new AutoValue_StatisticsViewState.Builder().isLoading(false)
                .activeCount(0)
                .completedCount(0)
                .error(null)
                .build();
    }


    public abstract Builder buildWith();

    @AutoValue.Builder
    static abstract class Builder {
        abstract Builder isLoading(boolean isLoading);
        abstract Builder activeCount(int activeCount);
        abstract Builder completedCount(int completedCount);
        abstract Builder error(@Nullable Throwable error);
        abstract StatisticsViewState build();

    }}

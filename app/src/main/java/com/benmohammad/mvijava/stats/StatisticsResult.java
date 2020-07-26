package com.benmohammad.mvijava.stats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.mvijava.mvibase.MviResult;
import com.benmohammad.mvijava.util.LceStatus;
import com.google.auto.value.AutoValue;

public interface StatisticsResult extends MviResult {

    @AutoValue
    abstract class LoadStatistics implements StatisticsResult {
        @NonNull
        abstract LceStatus status();

        abstract int activeCount();

        abstract int completedCount();

        @Nullable
        abstract Throwable error();

        @NonNull
        static LoadStatistics success(int activeCount, int completedCount) {
            return new AutoValue_StatisticsResult_LoadStatistics(LceStatus.SUCCESS, activeCount, completedCount, null);
        }

        @NonNull
        static LoadStatistics failure(Throwable error) {
            return new AutoValue_StatisticsResult_LoadStatistics(LceStatus.FAILURE, 0, 0, error);
        }

        @NonNull
        static LoadStatistics inFlight() {
            return new AutoValue_StatisticsResult_LoadStatistics(LceStatus.IN_FLIGHT, 0, 0, null);
        }
    }
}

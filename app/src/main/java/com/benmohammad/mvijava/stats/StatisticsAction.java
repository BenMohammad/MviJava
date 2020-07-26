package com.benmohammad.mvijava.stats;

import com.benmohammad.mvijava.mvibase.MviAction;
import com.google.auto.value.AutoValue;

public interface StatisticsAction extends MviAction {

    @AutoValue
    abstract class LoadStatistics implements StatisticsAction {
        public static LoadStatistics create() {
            return new AutoValue_StatisticsAction_LoadStatistics();

        }
    }}

package com.benmohammad.mvijava.tasks;

import androidx.annotation.Nullable;

import com.benmohammad.mvijava.data.Task;
import com.benmohammad.mvijava.mvibase.MviViewState;
import com.google.auto.value.AutoValue;

import java.util.Collections;
import java.util.List;

import static com.benmohammad.mvijava.tasks.TasksFilterType.ALL_TASKS;

@AutoValue
public abstract class TasksViewState implements MviViewState {

    public abstract boolean isLoading();
    public abstract TasksFilterType tasksFilterType();
    public abstract List<Task> tasks();

    @Nullable
    abstract Throwable error();

    public abstract boolean taskComplete();
    public abstract boolean taskActivated();
    public abstract boolean completedTasksCleared();

    public abstract Builder buildWith();

    static TasksViewState idle() {
        return new AutoValue_TasksViewState.Builder().isLoading(false)
                .tasksFilterType(ALL_TASKS)
                .tasks(Collections.emptyList())
                .error(null)
                .taskComplete(false)
                .taskActivated(false)
                .completedTasksCleared(false)
                .build();
    }

    @AutoValue.Builder
    static abstract class Builder {
        abstract Builder isLoading(boolean isLoading);
        abstract Builder tasksFilterType(TasksFilterType tasksFilterType);
        abstract Builder tasks(@Nullable List<Task> tasks);
        abstract Builder error(@Nullable Throwable error);
        abstract Builder taskComplete(boolean taskComplete);
        abstract Builder taskActivated(boolean taskActivated);
        abstract Builder completedTasksCleared(boolean completedTasksCleared);

        abstract TasksViewState build();
    }


}

package com.benmohammad.mvijava.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.mvijava.data.Task;
import com.benmohammad.mvijava.mvibase.MviResult;
import com.benmohammad.mvijava.util.LceStatus;
import com.benmohammad.mvijava.util.UiNotificationStatus;
import com.google.auto.value.AutoValue;

import java.util.List;

import static com.benmohammad.mvijava.util.LceStatus.FAILURE;
import static com.benmohammad.mvijava.util.LceStatus.IN_FLIGHT;
import static com.benmohammad.mvijava.util.LceStatus.SUCCESS;
import static com.benmohammad.mvijava.util.UiNotificationStatus.HIDE;
import static com.benmohammad.mvijava.util.UiNotificationStatus.SHOW;

public interface TasksResult extends MviResult {

    @AutoValue
    abstract class LoadTasks implements TasksResult {
        @NonNull
        abstract LceStatus status();

        @Nullable
        abstract List<Task> tasks();

        @Nullable
        abstract TasksFilterType filterType();

        @Nullable
        abstract Throwable error();

        @NonNull
        static LoadTasks success(@NonNull List<Task> tasks, @Nullable TasksFilterType filterType) {
            return new AutoValue_TasksResult_LoadTasks(SUCCESS, tasks, filterType, null);
        }

        @NonNull
        static LoadTasks failure(Throwable error) {
            return new AutoValue_TasksResult_LoadTasks(FAILURE, null, null, error);
        }

        @NonNull
        static LoadTasks inFlight() {
            return new AutoValue_TasksResult_LoadTasks(IN_FLIGHT, null, null, null);
        }
    }

    @AutoValue
    abstract class ActivateTaskResult implements TasksResult {
        @NonNull
        abstract LceStatus status();

        @Nullable
        abstract UiNotificationStatus uiNotificationStatus();

        @Nullable
        abstract List<Task> tasks();

        @Nullable
        abstract Throwable error();

        @NonNull
        static ActivateTaskResult hideUiNotification() {
            return new AutoValue_TasksResult_ActivateTaskResult(SUCCESS, HIDE, null, null);
        }

        @NonNull
        static ActivateTaskResult success(@NonNull List<Task> tasks) {
            return new AutoValue_TasksResult_ActivateTaskResult(SUCCESS, SHOW, tasks, null);
        }

        @NonNull
        static ActivateTaskResult failure(Throwable error) {
            return new AutoValue_TasksResult_ActivateTaskResult(FAILURE, null, null, error);
        }

        @NonNull
        static ActivateTaskResult inFlight() {
            return new AutoValue_TasksResult_ActivateTaskResult(IN_FLIGHT, null, null, null);
        }
    }

    @AutoValue
    abstract class CompleteTaskResult implements TasksResult {
        @NonNull
        abstract LceStatus status();

        @Nullable
        abstract UiNotificationStatus uiNotificationStatus();

        @Nullable
        abstract List<Task> tasks();

        @Nullable
        abstract Throwable error();

        @NonNull
        static CompleteTaskResult hideUiNotification() {
            return new AutoValue_TasksResult_CompleteTaskResult(SUCCESS, HIDE, null, null);
        }

        @NonNull
        static CompleteTaskResult success(@NonNull List<Task> tasks) {
            return new AutoValue_TasksResult_CompleteTaskResult(SUCCESS, SHOW, tasks, null);
        }

        @NonNull
        static CompleteTaskResult failure(Throwable error) {
            return new AutoValue_TasksResult_CompleteTaskResult(FAILURE, null, null, error);
        }

        @NonNull
        static CompleteTaskResult inFlight() {
            return new AutoValue_TasksResult_CompleteTaskResult(IN_FLIGHT, null, null, null);
        }
    }

    @AutoValue
    abstract class ClearCompletedTasksResult implements TasksResult {
        @NonNull
        abstract LceStatus status();

        @Nullable
        abstract UiNotificationStatus uiNotificationStatus();

        @Nullable
        abstract List<Task> tasks();

        @Nullable
        abstract Throwable error();

        @NonNull
        static ClearCompletedTasksResult hideUiNotification() {
            return new AutoValue_TasksResult_ClearCompletedTasksResult(SUCCESS, HIDE, null, null);
        }

        @NonNull
        static ClearCompletedTasksResult success(@NonNull List<Task> tasks) {
            return new AutoValue_TasksResult_ClearCompletedTasksResult(SUCCESS, SHOW, tasks, null);
        }

        @NonNull
        static ClearCompletedTasksResult failure(Throwable error) {
            return new AutoValue_TasksResult_ClearCompletedTasksResult(FAILURE, null, null, error);
        }

        @NonNull
        static ClearCompletedTasksResult inFlight() {
            return new AutoValue_TasksResult_ClearCompletedTasksResult(IN_FLIGHT, null, null, null);
        }
    }


}

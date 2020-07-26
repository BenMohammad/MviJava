package com.benmohammad.mvijava.addedittask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.mvijava.data.Task;
import com.benmohammad.mvijava.mvibase.MviResult;
import com.benmohammad.mvijava.util.LceStatus;
import com.google.auto.value.AutoValue;

import static com.benmohammad.mvijava.util.LceStatus.FAILURE;
import static com.benmohammad.mvijava.util.LceStatus.IN_FLIGHT;
import static com.benmohammad.mvijava.util.LceStatus.SUCCESS;

public interface AddEditTaskResult extends MviResult {

    @AutoValue
    abstract class PopulateTask implements AddEditTaskResult {
        @NonNull
        abstract LceStatus status();

        @Nullable
        abstract Task task();

        @Nullable
        abstract Throwable error();

        @NonNull
        public static PopulateTask success(@NonNull Task task) {
            return new AutoValue_AddEditTaskResult_PopulateTask(SUCCESS, task, null);
        }


        @NonNull
        static PopulateTask failure(Throwable error) {
            return new AutoValue_AddEditTaskResult_PopulateTask(FAILURE, null, error);
        }

        @NonNull
        static PopulateTask inFlight() {
            return new AutoValue_AddEditTaskResult_PopulateTask(IN_FLIGHT, null, null);
        }
    }

    @AutoValue
    abstract class CreateTask implements AddEditTaskResult {
        abstract boolean isEmpty();

        static CreateTask success() {
            return new AutoValue_AddEditTaskResult_CreateTask(false);
        }

        static CreateTask empty() {
            return new AutoValue_AddEditTaskResult_CreateTask(true);
        }
    }

    @AutoValue
    abstract class UpdateTask implements AddEditTaskResult {
        static UpdateTask create() {
            return new AutoValue_AddEditTaskResult_UpdateTask();

        }
    }
}

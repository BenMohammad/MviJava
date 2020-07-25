package com.benmohammad.mvijava.addedittask;

import androidx.annotation.NonNull;

import com.benmohammad.mvijava.mvibase.MviAction;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;

public interface AddEditTaskAction extends MviAction {

    @AutoValue
    abstract class PopulateTask implements AddEditTaskAction {
        abstract String taskId();

        public static PopulateTask create(@Nonnull String taskId) {
            return new AutoValue_AddEditTaskAction_PopulateTask(taskId);
        }
    }

    @AutoValue
    abstract class CreateTask implements AddEditTaskAction {
        abstract String title();
        abstract String description();

        public static CreateTask create(@NonNull String title, @NonNull String description) {
            return new AutoValue_AddEditTaskAction_CreateTask(title, description);
        }
    }

    @AutoValue
    abstract class UpdateTask implements AddEditTaskAction {
        abstract String taskId();
        abstract String title();
        abstract String description();

        public static UpdateTask create(@Nonnull String taskId, @Nonnull String title, @NonNull String description) {
            return new AutoValue_AddEditTaskAction_UpdateTask(taskId, title, description);
        }
    }

    @AutoValue
    abstract class SkipMe implements AddEditTaskAction {
        public static SkipMe create() {
            return new AutoValue_AddEditTaskAction_SkipMe();
        }

    }

}

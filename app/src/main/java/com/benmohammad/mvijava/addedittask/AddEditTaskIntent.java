package com.benmohammad.mvijava.addedittask;

import androidx.annotation.NonNull;

import com.benmohammad.mvijava.mvibase.MviIntent;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

public interface AddEditTaskIntent extends MviIntent {

    @AutoValue
    abstract class InitialIntent implements AddEditTaskIntent {
        @Nullable
        abstract String taskId();

        public static InitialIntent create(@Nullable String taskId) {
            return new AutoValue_AddEditTaskIntent_InitialIntent(taskId);
        }
    }

    @AutoValue
    abstract class SaveTask implements AddEditTaskIntent {
        @Nullable
        abstract String taskId();

        abstract String title();

        abstract String description();

        public static SaveTask create(@Nullable String taskId, @NonNull String title, @NonNull String description) {
            return new AutoValue_AddEditTaskIntent_SaveTask(taskId, title, description);
        }
    }
}

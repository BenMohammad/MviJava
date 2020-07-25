package com.benmohammad.mvijava.taskdetail;

import androidx.annotation.Nullable;

import com.benmohammad.mvijava.mvibase.MviIntent;
import com.google.auto.value.AutoValue;

public interface TaskDetailIntent extends MviIntent {

    @AutoValue
    abstract class InitialIntent implements TaskDetailIntent {
        @Nullable
        abstract String taskId();

        public static InitialIntent create(@Nullable String taskId) {
            return new AutoValue_TaskDetailIntent_InitialIntent(taskId);
        }
    }

    @AutoValue
    abstract class DeleteTask implements TaskDetailIntent {
        abstract String taskId();

        public static DeleteTask create(String taskId) {
            return new AutoValue_TaskDetailIntent_DeleteTask(taskId);
        }
    }

    @AutoValue
    abstract class ActivateTaskIntent implements TaskDetailIntent {
        abstract String taskId();

        public static ActivateTaskIntent create(String taskId) {
            return new AutoValue_TaskDetailIntent_ActivateTaskIntent(taskId);
        }
    }
    @AutoValue
    abstract class CompleteTaskIntent implements TaskDetailIntent {
        abstract String taskId();

        public static CompleteTaskIntent create(String taskId) {
            return new AutoValue_TaskDetailIntent_CompleteTaskIntent(taskId);

        }
    }
}

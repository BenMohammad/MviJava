package com.benmohammad.mvijava.injection;

import android.content.Context;

import com.benmohammad.mvijava.data.source.TasksRepository;
import com.benmohammad.mvijava.data.source.local.TasksLocalDataSource;
import com.benmohammad.mvijava.data.source.remote.TasksRemoteDataSource;
import com.benmohammad.mvijava.util.schedulers.BaseSchedulerProvider;
import com.benmohammad.mvijava.util.schedulers.SchedulerProvider;

import javax.annotation.Nonnull;

import static kotlin.jvm.internal.Intrinsics.checkNotNull;

public class Injection {

    public static TasksRepository provideTasksRepository(@Nonnull Context context) {
        checkNotNull(context);
        return TasksRepository.getInstance(TasksRemoteDataSource.getInstance(),
                TasksLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }
}

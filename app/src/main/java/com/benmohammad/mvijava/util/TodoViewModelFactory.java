package com.benmohammad.mvijava.util;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.mvijava.injection.Injection;
import com.benmohammad.mvijava.taskdetail.TaskDetailActionProcessorHolder;
import com.benmohammad.mvijava.taskdetail.TaskDetailViewModel;
import com.benmohammad.mvijava.tasks.TasksActionProcessorHolder;
import com.benmohammad.mvijava.tasks.TasksViewModel;

public class TodoViewModelFactory implements ViewModelProvider.Factory {

    @SuppressLint("StaticFieldLeak")
    private static TodoViewModelFactory INSTANCE;

    private final Context context;

    private TodoViewModelFactory(Context context) {
        this.context = context;
    }

    public static TodoViewModelFactory getInstance(Context context) {
        if(INSTANCE == null) {
            INSTANCE = new TodoViewModelFactory(context);
        }
        return INSTANCE;
    }



    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass == TasksViewModel.class) {
            return (T) new TasksViewModel(
                    new TasksActionProcessorHolder(
                            Injection.provideTasksRepository(context),
                            Injection.provideSchedulerProvider()));

        } else if(modelClass == TaskDetailViewModel.class) {
            return (T) new TaskDetailViewModel(
                    new TaskDetailActionProcessorHolder(
                            Injection.provideTasksRepository(context),
                            Injection.provideSchedulerProvider()));
        }
        throw new IllegalArgumentException("Unknown model class: " + modelClass);
    }
}

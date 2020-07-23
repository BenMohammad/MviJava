package com.benmohammad.mvijava.data.source;

import androidx.annotation.NonNull;

import com.benmohammad.mvijava.data.Task;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface TasksDataSource {

    default Single<List<Task>> getTasks(boolean forceUpdate) {
        if(forceUpdate) refreshTasks();
        return getTasks();
    }

    Single<List<Task>> getTasks();
    Single<Task> getTask(@NonNull String taskId);
    Completable saveTAsk(@NonNull Task task);
    Completable completeTask(@NonNull Task task);
    Completable completeTask(@NonNull String taskId);
    Completable activateTask(@NonNull Task task);
    Completable activateTask(@NonNull String taskId);
    Completable clearCompletedTasks();
    void refreshTasks();
    void deleteAllTask();
    Completable deleteTask(@NonNull String taskId);

}

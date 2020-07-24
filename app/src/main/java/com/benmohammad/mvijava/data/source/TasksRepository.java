package com.benmohammad.mvijava.data.source;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.benmohammad.mvijava.data.Task;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.google.common.base.Preconditions.checkNotNull;


public class TasksRepository implements TasksDataSource {

    @Nullable
    private static TasksRepository INSTANCE = null;

    @NonNull
    private final TasksDataSource mTaskRemoteDataSource;

    @NonNull
    private final TasksDataSource mTaskLocalDataSource;

    @VisibleForTesting
    @Nullable
    Map<String, Task> mCachedTasks;

    @VisibleForTesting
    boolean mCacheIsDirty;

    private TasksRepository(@NonNull TasksDataSource taskRemoteDataSource,
                            @NonNull TasksDataSource taskLocalDataSource) {
        mTaskRemoteDataSource = checkNotNull(taskRemoteDataSource);
        mTaskLocalDataSource = checkNotNull(taskLocalDataSource);
    }

    public static TasksRepository getInstance(@NonNull TasksDataSource taskRemoteDataSource,
                                              @NonNull TasksDataSource taskLocalDataSource) {
        if(INSTANCE == null) {
            INSTANCE = new TasksRepository(taskRemoteDataSource, taskLocalDataSource);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Single<List<Task>> getTasks() {
        if(mCachedTasks != null && !mCacheIsDirty) {
            return Observable.fromIterable(mCachedTasks.values()).toList();
        } else {
            mCachedTasks = new LinkedHashMap<>();
        }

        Single<List<Task>> remoteTasks = getAndSaveRemoteTasks();
        if(mCacheIsDirty) {
            return remoteTasks;
        } else {
            Single<List<Task>> localTasks = getAndCacheLocalTasks();
            return Single.concat(localTasks, remoteTasks)
                    .filter(tasks -> !tasks.isEmpty())
                    .firstOrError();
        }
    }

    private Single<List<Task>> getAndCacheLocalTasks() {
        return mTaskLocalDataSource.getTasks()
                .flatMap(tasks -> Observable.fromIterable(tasks)
                .doOnNext(task -> mCachedTasks.put(task.getId(), task))
                        .toList());
    }

    private Single<List<Task>> getAndSaveRemoteTasks() {
        return mTaskRemoteDataSource.getTasks()
                .flatMap(tasks -> Observable.fromIterable(tasks).doOnNext(task -> {
                    mTaskLocalDataSource.saveTAsk(task);
                    mCachedTasks.put(task.getId(), task);
                }).toList())
                        .doOnSuccess(ignored -> mCacheIsDirty =  false);
    }

    @Override
    public Single<Task> getTask(@NonNull String taskId) {
        checkNotNull(taskId);

        final Task cachedTask = getTaskWithId(taskId);

        if(cachedTask == null) {
            mCachedTasks = new LinkedHashMap<>();
        }

        Single<Task> localTask = getTaskWithIdFromLocalRepository(taskId);
        Single<Task> remoteTask = mTaskRemoteDataSource.getTask(taskId).doOnSuccess(task -> {
            mTaskLocalDataSource.saveTAsk(task);
            mCachedTasks.put(task.getId(), task);
        });

                return Single.concat(localTask, remoteTask).firstOrError();
    }





    @Nullable
    private Task getTaskWithId(String id) {
        checkNotNull(id);
        if(mCachedTasks == null && mCachedTasks.isEmpty()) {
            return null;
        } else {
            return mCachedTasks.get(id);
        }
    }

    @NonNull
    Single<Task> getTaskWithIdFromLocalRepository(@NonNull final String taskId) {
        return mTaskLocalDataSource.getTask(taskId)
                .doOnSuccess(task -> mCachedTasks.put(task.getId(), task));
    }

    @Override
    public Completable saveTAsk(@NonNull Task task) {
        checkNotNull(task);
        mTaskRemoteDataSource.saveTAsk(task);
        mTaskLocalDataSource.saveTAsk(task);

        if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }

        mCachedTasks.put(task.getId(), task);
        return Completable.complete();
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        checkNotNull(task);
        mTaskRemoteDataSource.completeTask(task);
        mTaskLocalDataSource.completeTask(task);

        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);

        if(mCachedTasks == null) {
            mCachedTasks =  new LinkedHashMap<>();
        }

        mCachedTasks.put(task.getId(), completedTask);
        return Completable.complete();
    }

    @Override
    public Completable completeTask(@NonNull String taskId) {
        checkNotNull(taskId);
        Task taskWithId = getTaskWithId(taskId);
        if(taskWithId != null) {
            return completeTask(taskWithId);
        } else {
            return Completable.complete();
        }
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        checkNotNull(task);
        mTaskRemoteDataSource.activateTask(task);
        mTaskLocalDataSource.activateTask(task);

        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId(), false);
        if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }

        mCachedTasks.put(task.getId(), activeTask);
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        checkNotNull(taskId);

         Task taskWithId = getTaskWithId(taskId);
        if(taskWithId != null) {
            return activateTask(taskWithId);
        } else {
            return Completable.complete();
        }
    }

    @Override
    public Completable clearCompletedTasks() {
        mTaskRemoteDataSource.clearCompletedTasks();
        mTaskLocalDataSource.clearCompletedTasks();

        if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }

        Iterator<Map.Entry<String, Task>> it = mCachedTasks.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if(entry.getValue().isCompleted()) {
                it.remove();
            }
        }
        return Completable.complete();
    }

    @Override
    public void refreshTasks() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllTask() {
        mTaskRemoteDataSource.deleteAllTask();
        mTaskLocalDataSource.deleteAllTask();

        if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }

        mCachedTasks.clear();
    }

    @Override
    public Completable deleteTask(@NonNull String taskId) {
        mTaskRemoteDataSource.deleteTask(checkNotNull(taskId));
        mTaskLocalDataSource.deleteTask(checkNotNull(taskId));

        mCachedTasks.remove(taskId);
        return Completable.complete();


    }
}

package com.benmohammad.mvijava.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.mvijava.data.Task;
import com.benmohammad.mvijava.data.source.TasksDataSource;
import com.benmohammad.mvijava.util.schedulers.BaseSchedulerProvider;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;

import java.util.List;

import javax.annotation.Nonnegative;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static kotlin.jvm.internal.Intrinsics.checkNotNull;

public class TasksLocalDataSource implements TasksDataSource {

    @Nullable
    private static TasksLocalDataSource INSTANCE;

    @NonNull
    private final BriteDatabase mDatabaseHelper;

    @NonNull
    private Function<Cursor, Task> mTaskMapperFunction;

    private TasksLocalDataSource(@NonNull Context context,
                                 @NonNull BaseSchedulerProvider schedulerProvider) {

        checkNotNull(context, "Context is null");
        checkNotNull(schedulerProvider, "Scheduler provider is null");
        TasksDbHelper dbHelper = new TasksDbHelper(context);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        mDatabaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io());
        mTaskMapperFunction = this::getTask;
    }

    @NonNull
    private Task getTask(@NonNull Cursor c) {
        String itemId = c.getString(c.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID));
        String title = c.getString(c.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE));
        String description = c.getString(c.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION));
        boolean isCompleted = c.getInt(c.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED)) == 1;
        return new Task(title, description, itemId, isCompleted);
    }

    public static TasksLocalDataSource getInstance(@NonNull Context context,
                                                   @NonNull BaseSchedulerProvider schedulerProvider) {
        if(INSTANCE == null) {
            INSTANCE = new TasksLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }


    @Override
    public Single<List<Task>> getTasks() {
        String[] projection = {
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED
        };
        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), TasksPersistenceContract.TaskEntry.TABLE_NAME);
        return mDatabaseHelper.createQuery(TasksPersistenceContract.TaskEntry.TABLE_NAME, sql)
                .mapToList(mTaskMapperFunction)
                .firstOrError();
    }

    @Override
    public Single<Task> getTask(@NonNull String taskId) {
        String[] projection = {
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED
        };

        String sql = String.format("SELECT %s FROM %S WHERE %s LIKE ?", TextUtils.join(",", projection),
                TasksPersistenceContract.TaskEntry.TABLE_NAME, TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID);

        return mDatabaseHelper.createQuery(TasksPersistenceContract.TaskEntry.TABLE_NAME, sql, taskId)
                .mapToOne(mTaskMapperFunction)
                .firstOrError();

    }

    @Override
    public Completable saveTAsk(@NonNull Task task) {
        checkNotNull(task);
        ContentValues values = new  ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, task.getId());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, task.getDescription());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, task.isCompleted());
        mDatabaseHelper.insert(TasksPersistenceContract.TaskEntry.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
        return Completable.complete();
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        completeTask(task.getId());
        return null;
    }

    @Override
    public Completable completeTask(@NonNull String taskId) {
        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, true);
        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};
        mDatabaseHelper.update(TasksPersistenceContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        activateTask(task.getId());
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, false);

        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};
        mDatabaseHelper.update(TasksPersistenceContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        return Completable.complete();
    }

    @Override
    public Completable clearCompletedTasks() {
        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED + " LIKE ?";
        String[] selectionArgs = {"1"};
        mDatabaseHelper.delete(TasksPersistenceContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
        return null;
    }

    @Override
    public void refreshTasks() {

    }

    @Override
    public void deleteAllTask() {
        mDatabaseHelper.delete(TasksPersistenceContract.TaskEntry.TABLE_NAME, null);
    }

    @Override
    public Completable deleteTask(@NonNull String taskId) {
        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};
        mDatabaseHelper.delete(TasksPersistenceContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
        return Completable.complete();
    }
}

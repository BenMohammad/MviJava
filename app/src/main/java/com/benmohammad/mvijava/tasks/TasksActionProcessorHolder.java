package com.benmohammad.mvijava.tasks;

import androidx.annotation.NonNull;

import com.benmohammad.mvijava.data.source.TasksRepository;
import com.benmohammad.mvijava.util.schedulers.BaseSchedulerProvider;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;
import static com.benmohammad.mvijava.util.ObservableUtils.pairWithDelay;

public class TasksActionProcessorHolder {

    @NonNull
    private TasksRepository tasksRepository;
    @NonNull
    private BaseSchedulerProvider schedulerProvider;

    public TasksActionProcessorHolder(@NonNull TasksRepository tasksRepository,
                                      @NonNull BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        this.schedulerProvider = checkNotNull(schedulerProvider, "schedulerProvider cannot be null");
    }

    private ObservableTransformer<TasksAction.LoadTasks, TasksResult.LoadTasks> loadTaskProcessor =
            actions -> actions
                    .flatMap(action -> tasksRepository.getTasks(action.forceUpdate())
                    .toObservable()
                    .map(tasks -> TasksResult.LoadTasks.success(tasks, action.filterType()))
                    .onErrorReturn(TasksResult.LoadTasks::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.LoadTasks.inFlight()));

    public ObservableTransformer<TasksAction.ActivateTaskAction, TasksResult.ActivateTaskResult> activateTaskProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.activateTask(action.task())
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap(tasks ->
                            pairWithDelay(
                                    TasksResult.ActivateTaskResult.success(tasks),
                                    TasksResult.ActivateTaskResult.hideUiNotification()
                            ))
                    .onErrorReturn(TasksResult.ActivateTaskResult::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.ActivateTaskResult.inFlight()));

    private ObservableTransformer<TasksAction.CompleteTaskAction, TasksResult.CompleteTaskResult>  completeTaskProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.completeTask(action.task())
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap(tasks ->
                            pairWithDelay(
                                    TasksResult.CompleteTaskResult.success(tasks),
                                    TasksResult.CompleteTaskResult.hideUiNotification())
                    )
                    .onErrorReturn(TasksResult.CompleteTaskResult::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.CompleteTaskResult.inFlight()));

    private ObservableTransformer<TasksAction.ClearCompletedTasksAction, TasksResult.ClearCompletedTasksResult> clearCompletedTaskProcessor =
            actions -> actions.flatMap( action ->
                    tasksRepository.clearCompletedTasks()
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap(tasks ->
                            pairWithDelay(
                                    TasksResult.ClearCompletedTasksResult.success(tasks),
                                    TasksResult.ClearCompletedTasksResult.hideUiNotification()))
                    .onErrorReturn(TasksResult.ClearCompletedTasksResult::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.ClearCompletedTasksResult.inFlight()));

    ObservableTransformer<TasksAction, TasksResult> actionProcessor =
            actions -> actions.publish(shared -> Observable.merge(
                    shared.ofType(TasksAction.LoadTasks.class).compose(loadTaskProcessor),
                    shared.ofType(TasksAction.ActivateTaskAction.class).compose(activateTaskProcessor),
                    shared.ofType(TasksAction.CompleteTaskAction.class).compose(completeTaskProcessor),
                    shared.ofType(TasksAction.ClearCompletedTasksAction.class).compose(clearCompletedTaskProcessor))
                    .mergeWith(
                            shared.filter(v -> !(v instanceof TasksAction.LoadTasks)
                            && !(v instanceof TasksAction.ActivateTaskAction)
                            && !(v instanceof TasksAction.CompleteTaskAction)
                            && !(v instanceof TasksAction.ClearCompletedTasksAction))
                            .flatMap(w -> Observable.error(
                                    new IllegalArgumentException("Unknown Action type: " + w)))));

}

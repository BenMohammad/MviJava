package com.benmohammad.mvijava.taskdetail;

import androidx.annotation.NonNull;

import com.benmohammad.mvijava.data.source.TasksRepository;
import com.benmohammad.mvijava.taskdetail.TaskDetailAction.PopulateTask;
import com.benmohammad.mvijava.util.schedulers.BaseSchedulerProvider;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;
import static com.benmohammad.mvijava.util.ObservableUtils.pairWithDelay;

public class TaskDetailActionProcessorHolder {

    @NonNull
    private TasksRepository tasksRepository;

    @NonNull
    private BaseSchedulerProvider schedulerProvider;

    public TaskDetailActionProcessorHolder(@NonNull TasksRepository tasksRepository,
                                           @NonNull BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = checkNotNull(tasksRepository, "repo cannot be null");
        this.schedulerProvider = checkNotNull(schedulerProvider, "scheduler cannot be null");
    }

    private ObservableTransformer<TaskDetailAction.PopulateTask, TaskDetailResult.PopulateTask>
        populateTaskProcessor = actions -> actions.flatMap(action ->
            tasksRepository.getTask(action.taskId())
                            .toObservable()
            .map(TaskDetailResult.PopulateTask::success)
            .onErrorReturn(TaskDetailResult.PopulateTask::failure)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(TaskDetailResult.PopulateTask.inFlight()));

    private ObservableTransformer<TaskDetailAction.CompleteTask, TaskDetailResult.CompleteTaskResult>
        completeTaskProcessor = actions ->
            actions.flatMap(action ->
                    tasksRepository.completeTask(action.taskId())
                    .andThen(tasksRepository.getTask(action.taskId()))
                    .toObservable()
                    .flatMap(task ->
                            pairWithDelay(
                                    TaskDetailResult.CompleteTaskResult.success(task),
                                    TaskDetailResult.CompleteTaskResult.hideUiNotification()
                            )))
            .onErrorReturn(TaskDetailResult.CompleteTaskResult::failure)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(TaskDetailResult.CompleteTaskResult.inFlight());


    private ObservableTransformer<TaskDetailAction.ActivateTask, TaskDetailResult.ActivateTaskResult>
        activateTaskProcessor = actions ->
            actions.flatMap(action ->
                    tasksRepository.activateTask(action.taskId())
                    .andThen(tasksRepository.getTask(action.taskId())
                            .toObservable()
                            .flatMap(task ->
                                    pairWithDelay(
                                            TaskDetailResult.ActivateTaskResult.success(task),
                                            TaskDetailResult.ActivateTaskResult.hideUiNotification()))

                            .onErrorReturn(TaskDetailResult.ActivateTaskResult::failure)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .startWith(TaskDetailResult.ActivateTaskResult.inFlight())));


    private ObservableTransformer<TaskDetailAction.DeleteTask, TaskDetailResult.DeleteTaskResult>
        deleteTaskProcessor = actions ->
            actions.flatMap(action ->
                    tasksRepository.deleteTask(action.taskId())
                        .andThen(Observable.just(TaskDetailResult.DeleteTaskResult.success()))
                        .onErrorReturn(TaskDetailResult.DeleteTaskResult::failure)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .startWith(TaskDetailResult.DeleteTaskResult.inFlight()));


    ObservableTransformer<TaskDetailAction, TaskDetailResult> actionProcessor =
            actions -> actions.publish(shared -> Observable.merge(
                    shared.ofType(TaskDetailAction.PopulateTask.class).compose(populateTaskProcessor),
                    shared.ofType(TaskDetailAction.CompleteTask.class).compose(completeTaskProcessor),
                    shared.ofType(TaskDetailAction.ActivateTask.class).compose(activateTaskProcessor),
                    shared.ofType(TaskDetailAction.DeleteTask.class).compose(deleteTaskProcessor))
                .mergeWith(shared.filter(v -> !(v instanceof TaskDetailAction.PopulateTask)
                                        && !(v instanceof  TaskDetailAction.CompleteTask)
                                        && !(v instanceof TaskDetailAction.ActivateTask)
                                        && !(v instanceof TaskDetailAction.DeleteTask))
                        .flatMap(w -> Observable.error(
                                new IllegalArgumentException("Unknown Action type: "+ w)
                        ))));

}

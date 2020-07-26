package com.benmohammad.mvijava.addedittask;

import androidx.annotation.NonNull;

import com.benmohammad.mvijava.data.Task;
import com.benmohammad.mvijava.data.source.TasksRepository;
import com.benmohammad.mvijava.tasks.TasksResult;
import com.benmohammad.mvijava.util.schedulers.BaseSchedulerProvider;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class AddEditTaskActionProcessorHolder {

    @NonNull
    private TasksRepository tasksRepository;

    @NonNull
    private BaseSchedulerProvider schedulerProvider;

    public AddEditTaskActionProcessorHolder(@NonNull TasksRepository tasksRepository,
                                            @NonNull BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = checkNotNull(tasksRepository, "repo cannot be null");
        this.schedulerProvider = checkNotNull(schedulerProvider, "schedulerProvider cannot be null");
    }

    private ObservableTransformer<AddEditTaskAction.PopulateTask, AddEditTaskResult.PopulateTask>
        populateTaskProcessor = actions ->
            actions.flatMap(action ->
                    tasksRepository.getTask(action.taskId())
                        .toObservable()
                        .map(AddEditTaskResult.PopulateTask::success)
                        .onErrorReturn(AddEditTaskResult.PopulateTask::failure)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .startWith(AddEditTaskResult.PopulateTask.inFlight()));

    private ObservableTransformer<AddEditTaskAction.CreateTask, AddEditTaskResult.CreateTask>
        createTaskProcessor = actions ->
            actions.map(action -> {
                Task task = new Task(action.title(), action.description());
                if(task.isEmpty()) {
                    return AddEditTaskResult.CreateTask.empty();
                }
                tasksRepository.saveTAsk(task);
                return AddEditTaskResult.CreateTask.success();
            });

    private ObservableTransformer<AddEditTaskAction.UpdateTask, AddEditTaskResult.UpdateTask>
        updateTaskProcessor = actions ->
            actions.flatMap(action ->
                    tasksRepository.saveTAsk(new Task(action.title(), action.description(), action.taskId()))
            .andThen(Observable.just(AddEditTaskResult.UpdateTask.create())));


    ObservableTransformer<AddEditTaskAction, AddEditTaskResult> actionProcessor =
            actions -> actions.publish(shared -> Observable.merge(
                    shared.ofType(AddEditTaskAction.PopulateTask.class).compose(populateTaskProcessor),
                    shared.ofType(AddEditTaskAction.CreateTask.class).compose(createTaskProcessor),
                    shared.ofType(AddEditTaskAction.UpdateTask.class).compose(updateTaskProcessor))
                .mergeWith(
                        shared.filter(v -> !(v instanceof AddEditTaskAction.PopulateTask) &&
                                           !(v instanceof AddEditTaskAction.CreateTask) &&
                                           !(v instanceof AddEditTaskAction.UpdateTask))
                        .flatMap(w -> Observable.error(
                                new IllegalArgumentException("unknown Action type: " + w)
                        ))
                )
            );
}

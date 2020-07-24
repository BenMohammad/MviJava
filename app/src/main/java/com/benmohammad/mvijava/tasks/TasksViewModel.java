package com.benmohammad.mvijava.tasks;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.benmohammad.mvijava.data.Task;
import com.benmohammad.mvijava.mvibase.MviIntent;
import com.benmohammad.mvijava.mvibase.MviViewModel;
import com.benmohammad.mvijava.util.UiNotificationStatus;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class TasksViewModel extends ViewModel implements MviViewModel<TasksIntent, TasksViewState> {

    @NonNull
    private PublishSubject<TasksIntent> intentsSubject;

    @NonNull
    private Observable<TasksViewState> statesObservable;

    @Nonnull
    private CompositeDisposable disposables = new CompositeDisposable();

    @Nonnull
    private TasksActionProcessorHolder actionProcessorHolder;

    public TasksViewModel(@Nonnull TasksActionProcessorHolder tasksActionProcessorHolder) {
        this.actionProcessorHolder = checkNotNull(tasksActionProcessorHolder, "actionProcessHolder cannot ne null");
        intentsSubject = PublishSubject.create();
        statesObservable = compose();
    }


    @Override
    public void processIntents(Observable<TasksIntent> intents) {
        disposables.add(intents.subscribe(intentsSubject::onNext));
    }

    @Override
    public Observable<TasksViewState> states() {
        return statesObservable;
    }

    private Observable<TasksViewState> compose() {
        return intentsSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .compose(actionProcessorHolder.actionProcessor)
                .scan(TasksViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0);

    }

    private ObservableTransformer<TasksIntent, TasksIntent> intentFilter =
            intents -> intents.publish(shared ->
                    Observable.merge(
                            shared.ofType(TasksIntent.InitialIntent.class).take(1),
                            shared.filter(intent -> !(intent instanceof TasksIntent.InitialIntent))
                    ));


    private TasksAction actionFromIntent(MviIntent intent) {
        if (intent instanceof TasksIntent.InitialIntent) {
            return TasksAction.LoadTasks.loadAndFilter(true, TasksFilterType.ALL_TASKS);
        }
        if (intent instanceof TasksIntent.ChangeFilterIntent) {
            return TasksAction.LoadTasks.loadAndFilter(false, ((TasksIntent.ChangeFilterIntent) intent).filterType());
        }
        if (intent instanceof TasksIntent.RefreshIntent) {
            return TasksAction.LoadTasks.load(((TasksIntent.RefreshIntent) intent).forceUpdate());
        }
        if (intent instanceof TasksIntent.ActivateTaskIntent) {
            return TasksAction.ActivateTaskAction.create(
                    ((TasksIntent.ActivateTaskIntent) intent).task());
        }
        if (intent instanceof TasksIntent.CompleteTaskIntent) {
            return TasksAction.CompleteTaskAction.create(
                    ((TasksIntent.CompleteTaskIntent) intent).task());
        }
        if (intent instanceof TasksIntent.ClearCompletedTaskIntent) {
            return TasksAction.ClearCompletedTasksAction.create();
        }
        throw new IllegalArgumentException("do not know how to treat this intent: " + intent);
    }

    @Override
    protected void onCleared() {
        disposables.dispose();
    }

    private static BiFunction<TasksViewState, TasksResult, TasksViewState> reducer =
            (previousState, result) -> {
                TasksViewState.Builder stateBuilder = previousState.buildWith();
                if (result instanceof TasksResult.LoadTasks) {
                    TasksResult.LoadTasks loadResult = (TasksResult.LoadTasks) result;
                    switch (loadResult.status()) {
                        case SUCCESS:
                            TasksFilterType filterType = loadResult.filterType();
                            if (filterType == null) {
                                filterType = previousState.tasksFilterType();
                            }
                            List<Task> tasks = filteredTasks(checkNotNull(loadResult.tasks()), filterType);
                            return stateBuilder.isLoading(false).tasks(tasks).tasksFilterType(filterType).build();
                        case FAILURE:
                            return stateBuilder.isLoading(false).error(loadResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.isLoading(true).build();
                    }
                } else if(result instanceof TasksResult.CompleteTaskResult) {
                    TasksResult.CompleteTaskResult completedTaskResult =
                            (TasksResult.CompleteTaskResult) result;
                    switch(completedTaskResult.status()) {
                        case SUCCESS:
                            stateBuilder.taskComplete(completedTaskResult.uiNotificationStatus() == UiNotificationStatus.SHOW);
                            if(completedTaskResult.tasks() != null) {
                                List<Task> tasks =
                                        filteredTasks(checkNotNull(completedTaskResult.tasks()), previousState.tasksFilterType());
                                stateBuilder.tasks(tasks);
                            }
                            return stateBuilder.build();
                        case FAILURE:
                            return stateBuilder.error(completedTaskResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.build();
                    }
                } else if(result instanceof TasksResult.ActivateTaskResult) {
                    TasksResult.ActivateTaskResult activateTaskResult =
                            (TasksResult.ActivateTaskResult) result;
                    switch(activateTaskResult.status()) {
                        case SUCCESS:
                            stateBuilder.taskActivated(activateTaskResult.uiNotificationStatus() == UiNotificationStatus.SHOW);
                            if(activateTaskResult.tasks() != null) {
                               List<Task> tasks = filteredTasks(checkNotNull(activateTaskResult.tasks()), previousState.tasksFilterType());
                               stateBuilder.tasks(tasks);
                            }
                            return stateBuilder.build();
                        case FAILURE:
                            return stateBuilder.error(activateTaskResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.build();
                    }
                } else if(result instanceof TasksResult.ClearCompletedTasksResult) {
                    TasksResult.ClearCompletedTasksResult clearedCompletedTask =
                            (TasksResult.ClearCompletedTasksResult) result;
                    switch(clearedCompletedTask.status()) {
                        case SUCCESS:
                            stateBuilder.completedTasksCleared(clearedCompletedTask.uiNotificationStatus() == UiNotificationStatus.SHOW);
                            if(clearedCompletedTask.tasks() != null) {
                                List<Task> tasks = filteredTasks(checkNotNull(clearedCompletedTask.tasks()), previousState.tasksFilterType());
                                stateBuilder.tasks(tasks);
                            }
                            return stateBuilder.build();
                        case FAILURE:
                            return stateBuilder.error(clearedCompletedTask.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.build();
                    }
                } else {
                    throw new IllegalArgumentException("Dont know this result:" + result);
                }
                throw new IllegalArgumentException("Mishandled result? should not happen?");
    };


            private static List<Task> filteredTasks(@Nonnull List<Task> tasks,
                                                    @Nonnull TasksFilterType filterType) {

                        List<Task> filteredTasks = new ArrayList<>(tasks.size());
                        switch(filterType) {
                            case ALL_TASKS:
                                filteredTasks.addAll(tasks);
                                break;
                            case ACTIVE_TASKS:
                                for(Task task : tasks) {
                                    if(task.isActive()) filteredTasks.add(task);
                                }
                                break;
                            case COMPLETED_TASKS:
                                for(Task task : tasks) {
                                    if(task.isCompleted()) filteredTasks.add(task);
                                }
                                break;
                        }
                        return filteredTasks;

                            }}

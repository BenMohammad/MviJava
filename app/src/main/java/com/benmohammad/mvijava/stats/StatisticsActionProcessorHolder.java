package com.benmohammad.mvijava.stats;

import androidx.annotation.NonNull;

import com.benmohammad.mvijava.data.Task;
import com.benmohammad.mvijava.data.source.TasksRepository;
import com.benmohammad.mvijava.util.Pair;
import com.benmohammad.mvijava.util.schedulers.BaseSchedulerProvider;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class StatisticsActionProcessorHolder {

    @NonNull
    private TasksRepository tasksRepository;

    @NonNull
    private BaseSchedulerProvider schedulerProvider;

    public StatisticsActionProcessorHolder(@NonNull TasksRepository tasksRepository,
                                           @NonNull BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        this.schedulerProvider = checkNotNull(schedulerProvider, "schedulerProvider cannot be null");
    }

    private ObservableTransformer<StatisticsAction.LoadStatistics, StatisticsResult.LoadStatistics> loadStatisticsProcessor = actions ->
            actions.flatMap(action ->
                    tasksRepository.getTasks()
                    .toObservable()
                    .flatMap(Observable::fromIterable)
                    .publish(shared ->
                            Single.zip(
                                    shared.filter(Task::isActive).count(),
                                    shared.filter(Task::isCompleted).count(),
                                    Pair::create).toObservable())

                    .map(pair ->
                            StatisticsResult.LoadStatistics.success(
                                    pair.First().intValue(), pair.Second().intValue()))
                    .onErrorReturn(StatisticsResult.LoadStatistics::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(StatisticsResult.LoadStatistics.inFlight()));

    ObservableTransformer<StatisticsAction, StatisticsResult> actionProcessor =
            actions -> actions.publish(shared ->
                    shared.ofType(StatisticsAction.LoadStatistics.class).compose(loadStatisticsProcessor)
                    .cast(StatisticsResult.class).mergeWith(
                            shared.filter(v -> !(v instanceof  StatisticsAction.LoadStatistics))
                            .flatMap(w -> Observable.error(
                                    new IllegalArgumentException("Unknown action type: " + w)))));
}

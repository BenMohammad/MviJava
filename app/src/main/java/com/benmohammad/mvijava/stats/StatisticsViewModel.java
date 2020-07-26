package com.benmohammad.mvijava.stats;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.benmohammad.mvijava.mvibase.MviIntent;
import com.benmohammad.mvijava.mvibase.MviViewModel;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class StatisticsViewModel extends ViewModel implements MviViewModel<StatisticsIntent, StatisticsViewState> {

    @NonNull
    private PublishSubject<StatisticsIntent> intentSubject;

    @NonNull
    private Observable<StatisticsViewState> statesObservable;

    @NonNull
    private CompositeDisposable disposables = new CompositeDisposable();

    @NonNull
    private StatisticsActionProcessorHolder actionProcessorHolder;

    public StatisticsViewModel(@NonNull StatisticsActionProcessorHolder actionProcessorHolder) {
        this.actionProcessorHolder = checkNotNull(actionProcessorHolder, "actionProcessor cannot be null");
        intentSubject = PublishSubject.create();
        statesObservable = compose();
    }


    @Override
    public void processIntents(Observable<StatisticsIntent> intents) {
        disposables.add(intents.subscribe(intentSubject::onNext));
    }

    @Override
    public Observable<StatisticsViewState> states() {
        return statesObservable;
    }

    private Observable<StatisticsViewState> compose() {
        return intentSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .compose(actionProcessorHolder.actionProcessor)
                .scan(StatisticsViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0);

    }

    private ObservableTransformer<StatisticsIntent, StatisticsIntent> intentFilter =
            intents -> intents.publish(shared ->
                    Observable.merge(
                            shared.ofType(StatisticsIntent.InitialIntent.class).take(1),
                            shared.filter(intent -> !(intent instanceof StatisticsIntent.InitialIntent))
                    ));

    private StatisticsAction actionFromIntent(MviIntent intent) {
        if(intent instanceof StatisticsIntent.InitialIntent) {
            return StatisticsAction.LoadStatistics.create();
        }
        throw new IllegalArgumentException("do not know how to treat this intent: " + intent);
    }

    @Override
    protected void onCleared() {
        disposables.dispose();
    }

    private static BiFunction<StatisticsViewState, StatisticsResult, StatisticsViewState> reducer =
            (previousState, result) -> {
                StatisticsViewState.Builder stateBuilder = previousState.buildWith();
                if(result instanceof StatisticsResult.LoadStatistics) {
                    StatisticsResult.LoadStatistics loadResult = (StatisticsResult.LoadStatistics) result;
                    switch(loadResult.status()) {
                        case SUCCESS:
                            return stateBuilder.isLoading(false)
                                    .activeCount(loadResult.activeCount())
                                    .completedCount(loadResult.completedCount())
                                    .build();
                        case FAILURE:
                            return stateBuilder.isLoading(false).error(loadResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.isLoading(true).build();
                    }
                } else {
                    throw new IllegalArgumentException("Don't know this result:" + result);
                }
                throw new IllegalStateException("Mishandled result? should not happen......");
    };
}

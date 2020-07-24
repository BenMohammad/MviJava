package com.benmohammad.mvijava.mvibase;

import io.reactivex.Observable;

public interface MviViewModel<I extends MviIntent, S extends MviViewState> {

    void processIntents(Observable<I> intents);

    Observable<S> states();
}

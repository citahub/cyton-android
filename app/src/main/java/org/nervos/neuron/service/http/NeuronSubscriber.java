package org.nervos.neuron.service.http;

import rx.Subscriber;

public class NeuronSubscriber<T> extends Subscriber<T> {

    @Override
    public void onCompleted() {

    }
    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }
    @Override
    public void onNext(T t) {

    }
}

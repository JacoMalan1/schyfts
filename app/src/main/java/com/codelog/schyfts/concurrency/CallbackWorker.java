package com.codelog.schyfts.concurrency;

public class CallbackWorker implements Runnable {

    private final Callback callback;

    public CallbackWorker(Callback cb) {
        this.callback = cb;
    }

    @Override
    public void run() {
    }

    public void callback() {
        this.callback.callback();
    }

}

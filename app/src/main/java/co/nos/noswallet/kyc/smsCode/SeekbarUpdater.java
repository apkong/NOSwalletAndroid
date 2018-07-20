package co.nos.noswallet.kyc.smsCode;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.SerialDisposable;

public class SeekbarUpdater {

    public int getTimeout() {
        return TIMEOUT;
    }

    public interface Callbacks {
        void updateSeekbar(int secondsLeft);

        void onTimeout();
    }

    public static final int TIMEOUT = 90;

    SerialDisposable serialDisposable = null;

    @Inject
    SeekbarUpdater() {

    }

    public void destroy() {
        serialDisposable.dispose();
        serialDisposable = null;
    }

    public void startSeekbarTimeout(Callbacks callbacks) {
        if (serialDisposable == null) {
            serialDisposable = new SerialDisposable();
        }
        serialDisposable.set(
                Observable.interval(1, TimeUnit.SECONDS)
                        .map(it -> TIMEOUT - it.intValue())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {
                            if (it >= 0) {
                                if (callbacks != null) {
                                    callbacks.updateSeekbar(it);
                                }
                            } else {
                                if (callbacks != null) {
                                    callbacks.onTimeout();
                                }
                            }
                        }, throwable -> {

                        })
        );

    }
}

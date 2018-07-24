package co.nos.noswallet.base;

import android.os.Handler;
import android.os.Looper;

import javax.inject.Inject;

public class MainThreadEnsurer {

    private Handler handler;

    @Inject
    MainThreadEnsurer() {
    }

    public void runOnMainThread(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(runnable);
        }
    }


}

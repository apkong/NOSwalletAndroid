package co.nos.noswallet.push;

import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import io.reactivex.Single;

public class FirebasePushMessagingRepository implements PushMessagingRepository {

    public FirebasePushMessagingRepository() {
    }


    @Override
    public Single<String> getToken() {
        return Single.create(emitter -> {
            final Task<InstanceIdResult> _task = FirebaseInstanceId.getInstance().getInstanceId();

            _task.addOnCompleteListener(task -> {

                if (!emitter.isDisposed()) {
                    if (!task.isSuccessful()) {
                        emitter.onError(task.getException());
                    }
                    // Get new Instance ID token
                    InstanceIdResult result = task.getResult();
                    if (result == null) {
                        emitter.onError(new NullPointerException("InstanceIdResult is null!"));
                    } else {
                        String token = task.getResult().getToken();
                        emitter.onSuccess(token);
                    }
                }
            });
        });
    }
}

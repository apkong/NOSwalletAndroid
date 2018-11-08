package co.nos.noswallet.push;

import io.reactivex.Single;

public interface PushMessagingRepository {
    Single<String> getToken();
}

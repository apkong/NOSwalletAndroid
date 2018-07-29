package co.nos.noswallet.di.application;

import android.content.Context;

import co.nos.noswallet.BuildConfig;
import co.nos.noswallet.db.CredentialsProvider;
import co.nos.noswallet.db.RandomFetchedRepresentativesProvider;
import co.nos.noswallet.db.RealmCredentialsProvider;
import co.nos.noswallet.db.RepresentativesProvider;
import co.nos.noswallet.network.NeuroApi;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.exception.ErrorDispatcher;
import dagger.Module;
import dagger.Provides;
import io.realm.Realm;

@Module
public class ApplicationModule {
    final Context mContext;

    public ApplicationModule(Context context) {
        mContext = context;
    }

    @Provides
    Context providesApplicationContext() {
        return mContext;
    }

    @Provides
    NeuroClient providesNeuroClient(ErrorDispatcher errorDispatcher) {
        return new NeuroClient(BuildConfig.CONNECTION_URL, NeuroApi.class, errorDispatcher);
    }
}

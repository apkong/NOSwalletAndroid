package co.nos.noswallet.di.analytics;

import android.content.Context;

import co.nos.noswallet.analytics.AnalyticsService;
import co.nos.noswallet.bus.Logout;
import co.nos.noswallet.bus.RxBus;
import co.nos.noswallet.db.Migration;
import co.nos.noswallet.di.activity.ActivityScope;
import co.nos.noswallet.di.application.ApplicationScope;
import co.nos.noswallet.di.persistence.PersistenceModule;
import co.nos.noswallet.util.SharedPreferencesUtil;
import co.nos.noswallet.util.Vault;
import dagger.Module;
import dagger.Provides;
import io.realm.Realm;

@Module(includes = PersistenceModule.class)
public class AnalyticsModule {
    @Provides
    @ApplicationScope
    AnalyticsService providesAnalyticsService(Context context, Realm realm) {
        return new AnalyticsService(context.getApplicationContext(), realm);
    }
}

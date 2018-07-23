package co.nos.noswallet.di.application;


import javax.inject.Named;

import co.nos.noswallet.analytics.AnalyticsService;
import co.nos.noswallet.di.analytics.AnalyticsModule;
import co.nos.noswallet.di.persistence.PersistenceModule;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.util.SharedPreferencesUtil;
import dagger.Component;
import io.realm.Realm;

@Component(modules = {ApplicationModule.class, PersistenceModule.class, AnalyticsModule.class})
@ApplicationScope
public interface ApplicationComponent {
    // persistence module
    SharedPreferencesUtil provideSharedPreferencesUtil();

    // database
    Realm provideRealm();

    AnalyticsService provideAnalyticsService();

    NeuroClient providesNeuroClient();

    // encryption key
    @Named("encryption_key")
    byte[] providesEncryptionKey();
}

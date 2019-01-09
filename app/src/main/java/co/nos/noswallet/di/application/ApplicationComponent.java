package co.nos.noswallet.di.application;


import javax.inject.Named;

import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.analytics.AnalyticsService;
import co.nos.noswallet.db.CredentialsProvider;
import co.nos.noswallet.db.RepresentativesProvider;
import co.nos.noswallet.di.analytics.AnalyticsModule;
import co.nos.noswallet.di.persistence.PersistenceModule;
import co.nos.noswallet.model.NeuroWallet;
import co.nos.noswallet.network.compression_stuff.ApiResponseMapper;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.ui.settings.addressBook.repository.AddressbookRepository;
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

    NeuroWallet provideNOSWallet();

    CredentialsProvider providesCredentialsProvider();

    RepresentativesProvider providesRepresentativesProvider();

    AddressbookRepository providesAddressbookRepository();

    ApiResponseMapper providesApiResponseMapper();

    // encryption key
    @Named("encryption_key")
    byte[] providesEncryptionKey();


    void inject(NOSApplication nosApplication);


}

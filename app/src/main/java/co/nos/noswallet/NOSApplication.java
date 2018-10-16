package co.nos.noswallet;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;

import com.github.ajalt.reprint.core.Reprint;

import javax.inject.Inject;

import co.nos.noswallet.di.activity.ActivityComponent;
import co.nos.noswallet.di.activity.ActivityModule;
import co.nos.noswallet.di.activity.DaggerActivityComponent;
import co.nos.noswallet.di.application.ApplicationComponent;
import co.nos.noswallet.di.application.ApplicationModule;
import co.nos.noswallet.di.application.DaggerApplicationComponent;
import co.nos.noswallet.model.NeuroWallet;
import co.nos.noswallet.network.interactor.GetPendingBlocksUseCase;
import co.nos.noswallet.util.Vault;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Any custom application logic can go here
 */

public class NOSApplication extends MultiDexApplication {

    static NOSApplication context;

    private NeuroWallet nosWallet = new NeuroWallet();

    public static NeuroWallet getNosWallet() {
        return getApplication(context).nosWallet;
    }


    private ApplicationComponent mApplicationComponent;

    @Inject
    GetPendingBlocksUseCase getPendingBlocksUseCase;

    public void onCreate() {
        super.onCreate();
        context = this;

        // initialize Realm database
        Realm.init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // create new instance of the application component (DI)
        mApplicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();


        // initialize vault
        Vault.initializeVault(this);
        generateEncryptionKey();

        // initialize fingerprint
        Reprint.initialize(this);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        mApplicationComponent.inject(this);

    }

    /**
     * generate an encryption key and store in the vault
     */
    private void generateEncryptionKey() {
        if (Vault.getVault().getString(Vault.ENCRYPTION_KEY_NAME, null) == null) {
            Vault.getVault()
                    .edit()
                    .putString(Vault.ENCRYPTION_KEY_NAME,
                            Base64.encodeToString(Vault.generateKey(), Base64.DEFAULT))
                    .apply();
        }
    }

    /**
     * Retrieve instance of application Dagger component
     *
     * @return ApplicationComponent
     */
    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    public static NOSApplication getApplication(Context context) {
        return (NOSApplication) context.getApplicationContext();
    }

    public static ActivityComponent createActivityComponent(FragmentActivity activity) {
        return DaggerActivityComponent
                .builder()
                .applicationComponent(NOSApplication.getApplication(activity).getApplicationComponent())
                .activityModule(new ActivityModule(activity))
                .build();
    }


}

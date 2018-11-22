package co.nos.noswallet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;

import com.github.ajalt.reprint.core.Reprint;

import co.nos.noswallet.di.activity.ActivityComponent;
import co.nos.noswallet.di.activity.ActivityModule;
import co.nos.noswallet.di.activity.DaggerActivityComponent;
import co.nos.noswallet.di.application.ApplicationComponent;
import co.nos.noswallet.di.application.ApplicationModule;
import co.nos.noswallet.di.application.DaggerApplicationComponent;
import co.nos.noswallet.model.NeuroWallet;
import co.nos.noswallet.util.Vault;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Any custom application logic can go here
 */

public class NOSApplication extends MultiDexApplication {

    static NOSApplication context;
    public PublishSubject<Boolean> restarts = PublishSubject.create();

    private NeuroWallet nosWallet = new NeuroWallet();

    public static NeuroWallet getNosWallet() {
        return getApplication(context).nosWallet;
    }

    public BehaviorSubject<String> fcmTokenSubject = BehaviorSubject.createDefault("");

    private ApplicationComponent mApplicationComponent;

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

        Disposable appRestartDisposable = restarts.subscribe(this::restartApp, this::restartApp);
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

    public static NOSApplication get() {
        return getApplication(context);
    }

    private void restartApp(Object stub) {
        Context baseContext = getBaseContext();

        Intent mStartActivity = new Intent(baseContext, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(baseContext, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) baseContext.getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        }
        System.exit(0);
    }


}

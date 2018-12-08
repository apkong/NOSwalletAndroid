package co.nos.noswallet;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;
import android.util.Log;

import com.github.ajalt.reprint.core.Reprint;
import com.scottyab.rootbeer.RootBeer;

import co.nos.noswallet.di.activity.ActivityComponent;
import co.nos.noswallet.di.activity.ActivityModule;
import co.nos.noswallet.di.activity.DaggerActivityComponent;
import co.nos.noswallet.di.application.ApplicationComponent;
import co.nos.noswallet.di.application.ApplicationModule;
import co.nos.noswallet.di.application.DaggerApplicationComponent;
import co.nos.noswallet.model.NeuroWallet;
import co.nos.noswallet.util.Vault;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Any custom application logic can go here
 */

public class NOSApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    public static final String TAG = NOSApplication.class.getSimpleName();

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

        if (new RootBeer(this).isRootedWithoutBusyBoxCheck()) {
            System.exit(0);
        }
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

    public void restartApp(Object stub) {
        restartApp();
    }

    public void restartApp() {
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

    public void restartMainActivity() {
        if (currentActivity != null) {

            Log.d(TAG, "restartMainActivity() called");
            Intent intent = new Intent(currentActivity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            currentActivity.finish();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
    }

    private Activity currentActivity;

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}

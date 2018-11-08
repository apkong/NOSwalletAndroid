package co.nos.noswallet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hwangjr.rxbus.annotation.Subscribe;

import java.util.UUID;

import javax.inject.Inject;

import co.nos.noswallet.analytics.AnalyticsEvents;
import co.nos.noswallet.analytics.AnalyticsService;
import co.nos.noswallet.bus.HideOverlay;
import co.nos.noswallet.bus.Logout;
import co.nos.noswallet.bus.OpenWebView;
import co.nos.noswallet.bus.RxBus;
import co.nos.noswallet.bus.SeedCreatedWithAnotherWallet;
import co.nos.noswallet.bus.ShowOverlay;
import co.nos.noswallet.di.activity.ActivityComponent;
import co.nos.noswallet.di.activity.ActivityModule;
import co.nos.noswallet.di.activity.DaggerActivityComponent;
import co.nos.noswallet.di.application.ApplicationComponent;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.model.NanoWallet;
import co.nos.noswallet.network.compression_stuff.ApiResponseMapper;
import co.nos.noswallet.network.interactor.GetBlocksInfoUseCase;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import co.nos.noswallet.push.HandlePushMessagesService;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.FragmentUtility;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.ui.home.HasWebsocketMachine;
import co.nos.noswallet.ui.home.v2.HistoryFragment;
import co.nos.noswallet.ui.intro.IntroLegalFragment;
import co.nos.noswallet.ui.intro.IntroNewWalletFragment;
import co.nos.noswallet.ui.intro.IntroWelcomeFragment;
import co.nos.noswallet.ui.webview.WebViewDialogFragment;
import co.nos.noswallet.util.SharedPreferencesUtil;
import io.reactivex.disposables.Disposable;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements WindowControl, ActivityWithComponent, HasWebsocketMachine {

    public static final String TAG = MainActivity.class.getSimpleName();

    private FragmentUtility mFragmentUtility;
    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    protected ActivityComponent mActivityComponent;
    private FrameLayout mOverlay;

    @Inject
    WebsocketMachine websocketMachine;

    @Inject
    Realm realm;

    @Inject
    GetBlocksInfoUseCase getBlocksInfoUseCase;

    @Inject
    NanoWallet nanoWallet;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Inject
    AnalyticsService analyticsService;

    @Inject
    ApiResponseMapper apiResponseMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disableScreenCapture();

        // build the activity component
        mActivityComponent = DaggerActivityComponent
                .builder()
                .applicationComponent(NOSApplication.getApplication(this).getApplicationComponent())
                .activityModule(new ActivityModule(this))
                .build();

        // perform dagger injections
        mActivityComponent.inject(this);

        // subscribe to bus
        RxBus.get().register(this);

        // set unique uuid (per app install)
        if (!sharedPreferencesUtil.hasAppInstallUuid()) {
            sharedPreferencesUtil.setAppInstallUuid(UUID.randomUUID().toString());
        }

        initUi();

        if (websocketMachine != null) {
            websocketMachine.handleClickedNotification(getIntent());
        }


        setupNotificationsChannel();


    }

    private void setupNotificationsChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_LOW));
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        websocketMachine.start();
        HandlePushMessagesService.start(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    Disposable D;

    private void disableScreenCapture() {

        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (D != null) {
            D.dispose();
        }
        // unregister from bus
        RxBus.get().unregister(this);

        // close realm connection
        if (realm != null) {
            realm.close();
            realm = null;
        }

        // close wallet so app can clean up
        if (nanoWallet != null) {
            nanoWallet.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initUi() {
        // set main content view
        setContentView(R.layout.activity_main);

        // create fragment utility instance
        mFragmentUtility = new FragmentUtility(getSupportFragmentManager());
        mFragmentUtility.setContainerViewId(R.id.container);

        // get overlay
        mOverlay = findViewById(R.id.overlay);

        // set up toolbar
        mToolbar = findViewById(R.id.toolbar);
        mToolbarTitle = findViewById(R.id.toolbar_title);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // get wallet seed if it exists
        Credentials credentials = realm.where(Credentials.class).findFirst();

        // initialize analytics
        analyticsService.start();
//        if (credentials != null && credentials.getHasAgreedToTracking()) {
//            analyticsService.start();
//        } else if (credentials != null && !credentials.getHasAnsweredAnalyticsQuestion()) {
//            analyticsService.startAnswersOnly(); // for legal
//        } else {
//            analyticsService.stop();
//        }

        if (credentials == null) {
            // if we don't have a wallet, start the intro
            mFragmentUtility.clearStack();
            mFragmentUtility.replace(new IntroWelcomeFragment());
        } else if (credentials.getHasCompletedLegalAgreements()) {
            mFragmentUtility.clearStack();
            if (sharedPreferencesUtil.getConfirmedSeedBackedUp()) {
                // go to home screen

//                mFragmentUtility.replace(HomeFragment.newInstance());
                mFragmentUtility.replace(HistoryFragment.newInstance());
            } else {
                // go to intro new wallet
                mFragmentUtility.replace(IntroNewWalletFragment.newInstance());
            }
        } else {
            mFragmentUtility.clearStack();
            mFragmentUtility.replace(IntroLegalFragment.newInstance());
        }
    }

    @Subscribe
    public void logOut(Logout logout) {
        analyticsService.track(AnalyticsEvents.LOG_OUT);

        // delete user seed data before logging out
        final RealmResults<Credentials> results = realm.where(Credentials.class).findAll();
        realm.executeTransaction(realm1 -> results.deleteAllFromRealm());

        // clear wallet
        nanoWallet.clear();

        // null out component
        mActivityComponent = null;

        sharedPreferencesUtil.setConfirmedSeedBackedUp(false);
        sharedPreferencesUtil.setFromNewWallet(false);

        // go to the welcome fragment
        getFragmentUtility().clearStack();
        getFragmentUtility().replace(new IntroWelcomeFragment(), FragmentUtility.Animation.CROSSFADE);
    }

    @Subscribe
    public void openWebView(OpenWebView openWebView) {
        WebViewDialogFragment
                .newInstance(openWebView.getUrl(), openWebView.getTitle() != null ? openWebView.getTitle() : "")
                .show(getFragmentUtility().getFragmentManager(), WebViewDialogFragment.TAG);
    }

    @Subscribe
    public void showOverlay(ShowOverlay showOverlay) {
        mOverlay.setVisibility(View.VISIBLE);
        mOverlay.setOnClickListener(view -> {
        });
    }

    @Subscribe
    public void hideOverlay(HideOverlay hideOverlay) {
        mOverlay.setVisibility(View.GONE);
    }

    @Subscribe
    public void seedCreatedWithAnotherWallet(SeedCreatedWithAnotherWallet seedCreatedWithAnotherWallet) {
        realm.executeTransaction(realm -> {
            Credentials credentials = realm.where(Credentials.class).findFirst();
            if (credentials != null) {
                credentials.setSeedIsSecure(true);
            }
        });
    }

    @Override
    public FragmentUtility getFragmentUtility() {
        return mFragmentUtility;
    }


    /**
     * Set the status bar to a particular color
     *
     * @param color color resource id
     */
    @Override
    public void setStatusBarColor(@ColorRes int color) {
        // we can only set it 5.x and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, color));
        }
    }

    @Override
    public void setDarkIcons(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    /**
     * Set visibility of app toolbar
     *
     * @param visible true if toolbar should be visible
     */
    @Override
    public void setToolbarVisible(boolean visible) {
        if (mToolbar != null) {
            mToolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Set title of the app toolbar
     *
     * @param title Title of the toolbar
     */
    @Override
    public void setTitle(String title) {
        if (mToolbarTitle != null) {
            mToolbarTitle.setText(title);
        }
        setToolbarVisible(true);
    }

    /**
     * Set title drawable of app toolbar
     *
     * @param drawable Drawable to show next to title on the toolbar
     */
    @Override
    public void setTitleDrawable(int drawable) {
        if (mToolbarTitle != null) {
            mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
        }
        setToolbarVisible(true);
    }

    @Override
    public void setBackEnabled(boolean enabled) {
        if (mToolbar != null) {
            if (enabled) {
                mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                mToolbar.setNavigationOnClickListener(view -> mFragmentUtility.pop());
            } else {
                mToolbar.setNavigationIcon(null);
                mToolbar.setNavigationOnClickListener(null);
            }
        }
    }

    @Override
    public ActivityComponent getActivityComponent() {
        if (mActivityComponent == null) {
            // build the activity component
            mActivityComponent = DaggerActivityComponent
                    .builder()
                    .applicationComponent(NOSApplication.getApplication(this).getApplicationComponent())
                    .activityModule(new ActivityModule(this))
                    .build();
        }
        return mActivityComponent;
    }

    @Override
    public ApplicationComponent getApplicationComponent() {
        return NOSApplication.getApplication(this).getApplicationComponent();
    }

    @Override
    public WebsocketMachine getWebsocketMachine() {
        return websocketMachine;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (websocketMachine != null) {
            websocketMachine.handleClickedNotification(intent);
        }
    }
}

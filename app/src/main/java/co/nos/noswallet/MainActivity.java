package co.nos.noswallet;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

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
import co.nos.noswallet.network.nosModel.GetBlocksInfoResponse;
import co.nos.noswallet.network.nosModel.GetPendingBlocksRequest;
import co.nos.noswallet.network.websockets.NosNodeWebSocketListener;
import co.nos.noswallet.network.websockets.WebsocketRunner;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.FragmentUtility;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.ui.home.HomeFragment;
import co.nos.noswallet.ui.intro.IntroLegalFragment;
import co.nos.noswallet.ui.intro.IntroNewWalletFragment;
import co.nos.noswallet.ui.intro.IntroWelcomeFragment;
import co.nos.noswallet.ui.webview.WebViewDialogFragment;
import co.nos.noswallet.util.SharedPreferencesUtil;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements WindowControl, ActivityWithComponent {

    public static final String TAG = MainActivity.class.getSimpleName();

    private FragmentUtility mFragmentUtility;
    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    protected ActivityComponent mActivityComponent;
    private FrameLayout mOverlay;

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


        setupWebSockets();
    }

    private void setupWebSockets() {

        String url = "wss:/backendtest.nosnode.net:8888/";

        WebsocketRunner runner = new WebsocketRunner(new OkHttpClient(),
                url, new NosNodeWebSocketListener());

        runner.init();

        String account = getBlocksInfoUseCase.provideAccountNumber(realm);

        runner.send(new GetPendingBlocksRequest(account,"1"));

        Disposable disposable = runner.observeMessages()
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println("onNext -> [" + s + "]");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.err.println("onError -> [" + throwable.getMessage() + "]");
                        throwable.printStackTrace();
                    }
                });

//        Log.d(TAG, "setupWebSockets() called");
//        OkHttpClient client = new OkHttpClient();
//
//        WebSocketListener listener = new WebSocketListener() {
//
//            final Charset utf8 = Charset.forName("UTF-8");
//
//            @Override
//            public void onOpen(WebSocket webSocket, Response response) {
//                super.onOpen(webSocket, response);
//                Log.w(TAG, "onOpen() called with: webSocket = [" + webSocket + "], response = [" + response + "]");
//                String request = "{\"currency\":\"usd\",\"action\":\"get_pow\", \"account\":\"xrb_3bgmpjak8j9c3muqk8u7ctr3qec4wdsdke3rgu958kmzbe4ehbjoihfxgdk9\"}";
////                webSocket.send(
////                        ByteString.decodeHex(apiResponseMapper.toHexString(
////                                apiResponseMapper.serialize(request)
////                                )
////                        )
////                );
//                webSocket.send(request);
//            }
//
//            @Override
//            public void onMessage(WebSocket webSocket, String text) {
//                super.onMessage(webSocket, text);
//               // byte[] response = apiResponseMapper.deserialize(text.getBytes());
//
//                Log.w(TAG, "onMessage1() called with: webSocket = [" + webSocket
//                        + "], text = [" + text + "]");
//            }
//
//            @Override
//            public void onMessage(WebSocket webSocket, ByteString bytes) {
//                super.onMessage(webSocket, bytes);
//                //byte[] response = apiResponseMapper.deserialize(bytes.toByteArray());
//
//                Log.w(TAG, "onMessage2() called with: webSocket = [" + webSocket + "], bytes = [" +
//                        bytes.string(utf8) + "]");
//            }
//
//            @Override
//            public void onClosing(WebSocket webSocket, int code, String reason) {
//                super.onClosing(webSocket, code, reason);
//                Log.e(TAG, "onClosing() called with: webSocket = [" + webSocket + "], code = [" + code + "], reason = [" + reason + "]");
//            }
//
//            @Override
//            public void onClosed(WebSocket webSocket, int code, String reason) {
//                super.onClosed(webSocket, code, reason);
//                Log.e(TAG, "onClosed() called with: webSocket = [" + webSocket + "], code = [" + code + "], reason = [" + reason + "]");
//            }
//
//            @Override
//            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
//                super.onFailure(webSocket, t, response);
//                Log.e(TAG, "onFailure() called with: webSocket = [" + webSocket + "], t = [" + t + "], response = [" + response + "]");
//            }
//        };
//
//        Request request = new Request.Builder().url(url).build();
//        WebSocket ws = client.newWebSocket(request, listener);
//
//        //client.dispatcher().executorService().shutdown();
    }

    static class ResponseDeserializer {
        static String deserialize(String s) {
            return "";
        }
    }

    static class ResponseSerializer {
        static String serialize(String s) {
            return "";
        }
    }

    private void disableScreenCapture() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop websocket on pause

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

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
                mFragmentUtility.replace(HomeFragment.newInstance());
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


}

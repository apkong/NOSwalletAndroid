package co.nos.noswallet.ui.intro;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import javax.inject.Inject;

import co.nos.noswallet.BuildConfig;
import co.nos.noswallet.NanoUtil;
import co.nos.noswallet.R;
import co.nos.noswallet.analytics.AnalyticsEvents;
import co.nos.noswallet.analytics.AnalyticsService;
import co.nos.noswallet.databinding.FragmentIntroWelcomeBinding;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseFragment;
import co.nos.noswallet.ui.common.FragmentUtility;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.util.SharedPreferencesUtil;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseFragment;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.util.SharedPreferencesUtil;
import io.realm.Realm;

/**
 * The Intro Screen to the app
 */

public class IntroWelcomeFragment extends BaseFragment {
    private FragmentIntroWelcomeBinding binding;
    public static String TAG = IntroWelcomeFragment.class.getSimpleName();

    @Inject
    Realm realm;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_intro_welcome, container, false);
        view = binding.getRoot();

        setStatusBarWhite(view);
        hideToolbar();

        // bind data to view
        binding.setVersion(getString(R.string.version_display, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        binding.setHandlers(new ClickHandlers());

        return view;
    }


    public class ClickHandlers {
        public void onClickNewWallet(View view) {
            // go to interstitial
            if (getActivity() instanceof WindowControl) {
                // create wallet seed
                realm.executeTransaction(realm -> {
                    Credentials credentials = realm.createObject(Credentials.class);
                    credentials.setSeed(NanoUtil.generateSeed());
                });

                sharedPreferencesUtil.setFromNewWallet(true);
                ((WindowControl) getActivity()).getFragmentUtility().replace(
                        IntroLegalFragment.newInstance(),
                        FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                        FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                        IntroLegalFragment.TAG
                );
            }
        }

        public void onClickHaveWallet(View view) {
            // let user input their existing wallet
            if (getActivity() instanceof WindowControl) {
                ((WindowControl) getActivity()).getFragmentUtility().add(
                        new IntroSeedFragment(),
                        FragmentUtility.Animation.CROSSFADE,
                        FragmentUtility.Animation.CROSSFADE,
                        IntroSeedFragment.TAG
                );
            }
        }
    }

}

package co.nos.noswallet.ui.home.v2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseFragment;
import co.nos.noswallet.ui.common.FragmentUtility;
import co.nos.noswallet.ui.common.KeyboardUtil;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.ui.receive.ReceiveDialogFragment;
import co.nos.noswallet.ui.send.SendCoinsFragment;
import co.nos.noswallet.ui.send.SendFragment;
import co.nos.noswallet.ui.settings.SettingsDialogFragment;
import io.realm.Realm;

public class HistoryFragment extends BaseFragment {

    public static String TAG = HistoryFragment.class.getSimpleName();

    public ViewPager viewPager;
    private PagerTabStrip pagerStrip;

    private CurrenciesPagerAdapter currenciesPagerAdapter;

    @Inject
    Realm realm;

    private final ViewPager.OnPageChangeListener onPageChangeCallback = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
//                visible zero position
            List<Fragment> fragmentsAttached = getChildFragmentManager().getFragments();
            for (Fragment f : fragmentsAttached) {
                if (f instanceof HasCurrency) {
                    CryptoCurrency cryptoCurrency = ((HasCurrency) f).getCurrency();
                    System.out.println("onPageSelected " + position + " with cryptoCurrency " + cryptoCurrency.name());
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return HomeFragment
     */
    public static HistoryFragment newInstance() {
        Bundle args = new Bundle();
        HistoryFragment fragment = new HistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.home_settings:
                if (getActivity() instanceof WindowControl) {
                    // show settings dialog
                    SettingsDialogFragment dialog = SettingsDialogFragment.newInstance();
                    dialog.show(((WindowControl) getActivity()).getFragmentUtility().getFragmentManager(),
                            SettingsDialogFragment.TAG);

                    // make sure that dialog is not null
                    ((WindowControl) getActivity()).getFragmentUtility().getFragmentManager().executePendingTransactions();

                    // reset status bar to blue when dialog is closed
                    dialog.getDialog().setOnDismissListener(dialogInterface -> {
                        setStatusBarBlue();
//                        if (binding.homeViewpager != null) {
//                            updateAmounts();
//                        }
                    });
                }
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle $) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

//        presenter.attachView(this);
        viewPager = view.findViewById(R.id.home_viewpager);
        pagerStrip = view.findViewById(R.id.pager_header);

        view.findViewById(R.id.home_receive_button).setOnClickListener(v -> {
            if (getActivity() instanceof WindowControl) {
                // show receive dialog
                ReceiveDialogFragment dialog = ReceiveDialogFragment.newInstance(
                        viewPager == null ? CryptoCurrency.NOLLAR : currencyForPosition(viewPager.getCurrentItem())
                );
                dialog.show(((WindowControl) getActivity()).getFragmentUtility().getFragmentManager(),
                        ReceiveDialogFragment.TAG);
            }
        });

        view.findViewById(R.id.home_send_button).setOnClickListener(v -> {
            if (getActivity() instanceof WindowControl) {
                // navigate to send screen

                ((WindowControl) getActivity()).getFragmentUtility().add(
                        SendCoinsFragment.newInstance(),
                        FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                        FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                        SendFragment.TAG
                );
            }
        });

        // set status bar to blue
        setTitle(getString(R.string.main_wallet));
        //setTitleDrawable(R.drawable.ic_launcher192);
        setBackEnabled(false);

        // hide keyboard
        KeyboardUtil.hideKeyboard(getActivity());

        Credentials credentials = realm.where(Credentials.class).findFirst();
//        if (credentials != null && !credentials.getHasAnsweredAnalyticsQuestion()) {
//            showAnalyticsOptIn(analyticsService, realm);
//        }

        if (credentials != null && !credentials.getSeedIsSecure() && !credentials.getHasSentToNewSeed()) {
            showSeedUpdateAlert();
        } else if (credentials != null && credentials.getNewlyGeneratedSeed() != null) {
            showSeedReminderAlert(credentials.getNewlyGeneratedSeed());
        }

        WebsocketMachine machine = WebsocketMachine.obtain(getActivity());
        if (machine != null) {
            machine.doAfterInit(machine::requestAccountHistoryForAll);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currenciesPagerAdapter = new CurrenciesPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(currenciesPagerAdapter);
        viewPager.addOnPageChangeListener(onPageChangeCallback);
    }

    public void showError(String string) {
        Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        viewPager.removeOnPageChangeListener(onPageChangeCallback);
        super.onDestroy();
    }

    static class CurrenciesPagerAdapter extends FragmentPagerAdapter {

        public CurrenciesPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return CurrencyFragment.newInstance(currencyForPosition(position));
        }

        @Override
        public int getCount() {
            return CryptoCurrency.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return currencyForPosition(position).name();
        }
    }

    public static CryptoCurrency currencyForPosition(int position) {
        if (position == 0) {
            return CryptoCurrency.NOLLAR;
        } else {
            return CryptoCurrency.NOS;
        }
    }
}

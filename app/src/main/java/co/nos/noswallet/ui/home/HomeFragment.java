package co.nos.noswallet.ui.home;

import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hwangjr.rxbus.annotation.Subscribe;

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.analytics.AnalyticsEvents;
import co.nos.noswallet.analytics.AnalyticsService;
import co.nos.noswallet.base.MainThreadEnsurer;
import co.nos.noswallet.bus.RxBus;
import co.nos.noswallet.bus.WalletHistoryUpdate;
import co.nos.noswallet.bus.WalletPriceUpdate;
import co.nos.noswallet.bus.WalletSubscribeUpdate;
import co.nos.noswallet.databinding.FragmentHomeBinding;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.model.NanoWallet;
import co.nos.noswallet.network.model.response.AccountHistoryResponseItem;
import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseDialogFragment;
import co.nos.noswallet.ui.common.BaseFragment;
import co.nos.noswallet.ui.common.FragmentUtility;
import co.nos.noswallet.ui.common.KeyboardUtil;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.ui.home.adapter.HistoryAdapter;
import co.nos.noswallet.ui.receive.ReceiveDialogFragment;
import co.nos.noswallet.ui.send.SendCoinsFragment;
import co.nos.noswallet.ui.send.SendFragment;
import co.nos.noswallet.ui.settings.SettingsDialogFragment;
import co.nos.noswallet.ui.webview.WebViewDialogFragment;
import io.realm.Realm;

/**
 * Home Wallet Screen
 */

@BindingMethods({
        @BindingMethod(type = android.support.v7.widget.AppCompatImageView.class,
                attribute = "srcCompat",
                method = "setImageDrawable")
})
public class HomeFragment extends BaseFragment implements HomeView {
    private FragmentHomeBinding binding;
    private WalletController controller;
    public static String TAG = HomeFragment.class.getSimpleName();
    private boolean logoutClicked = false;

    @Inject
    HomePresenter presenter;

    @Inject
    MainThreadEnsurer mainThreadEnsurer;

    @Inject
    NanoWallet wallet;

    @Inject
    AnalyticsService analyticsService;

    @Inject
    Realm realm;

    @Inject
    HistoryAdapter historyAdapter;

    ClickHandlers ClickHandlers;

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return HomeFragment
     */
    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
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
                        if (binding.homeViewpager != null) {
                            updateAmounts();
                        }
                    });
                }
                return true;
        }

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unregister from bus
        RxBus.get().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // init dependency injection

        presenter.attachView(this);

        setTitle(getString(R.string.main_wallet));

        analyticsService.track(AnalyticsEvents.HOME_VIEWED);

        // subscribe to bus
        RxBus.get().register(this);

        // set status bar to blue
        setTitle(getString(R.string.main_wallet));
        //setTitleDrawable(R.drawable.ic_launcher192);
        setBackEnabled(false);

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_home, container, false);
        view = binding.getRoot();

        // hide keyboard
        KeyboardUtil.hideKeyboard(getActivity());

        binding.setHandlers(ClickHandlers = new ClickHandlers());

        // initialize view pager (swipeable currency list)
        binding.homeViewpager.setAdapter(new CurrencyPagerAdapter(getContext(), wallet));
        binding.homeTabs.setupWithViewPager(binding.homeViewpager, true);

        // initialize recyclerview (list of wallet transactions)
        controller = new WalletController();
        binding.homeRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.homeRecyclerview.setAdapter(historyAdapter);
        binding.homeSwiperefresh.setOnRefreshListener(() -> {

        });
        if (wallet != null && wallet.getAccountHistory() != null) {
            controller.setData(wallet.getAccountHistory(), new ClickHandlers());
        }

        updateAmounts();

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
            machine.doAfterInit(() -> presenter.doOnResume(getActivity()));
        }

        return view;
    }

    @Subscribe
    public void receiveHistory(WalletHistoryUpdate walletHistoryUpdate) {
        controller.setData(wallet.getAccountHistory(), new ClickHandlers());
        binding.homeSwiperefresh.setRefreshing(false);
        binding.homeRecyclerview.getLayoutManager().scrollToPosition(0);
    }

    @Subscribe
    public void receivePrice(WalletPriceUpdate walletPriceUpdate) {
        updateAmounts();
    }

    @Subscribe
    public void receiveSubscribe(WalletSubscribeUpdate walletSubscribeUpdate) {
        updateAmounts();
    }

    private void updateAmounts() {
        if (wallet != null) {
            ((CurrencyPagerAdapter) binding.homeViewpager.getAdapter()).updateData(wallet);
            if (wallet.getAccountBalanceNanoRaw() != null &&
                    wallet.getAccountBalanceNanoRaw().compareTo(new BigDecimal(0)) == 1) {
                // if balance > 0, enable send button
                binding.homeSendButton.setEnabled(true);
            } else {
                //todo: uncomment later
                //binding.homeSendButton.setEnabled(false);
            }
        }
    }

    @Override
    public void showHistory(ArrayList<AccountHistory> history) {
        binding.homeSwiperefresh.setRefreshing(false);
        if (historyAdapter != null) {
            historyAdapter.refresh(history);
        }
        if (history.isEmpty()) {
            showHistoryEmpty();
        }
    }


    private void showHistoryEmpty() {
        showError(getString(R.string.error_history_empty));
    }

    @Override
    public void onBalanceFormattedReceived(String formatted) {

    }

    @Override
    public void showError(String string) {
        Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isNotAttached() {
        return getActivity() == null;
    }

    public class ClickHandlers {
        public void onClickReceive(View view) {
            if (getActivity() instanceof WindowControl) {
                // show receive dialog
                ReceiveDialogFragment dialog = ReceiveDialogFragment.newInstance();
                dialog.show(((WindowControl) getActivity()).getFragmentUtility().getFragmentManager(),
                        ReceiveDialogFragment.TAG);

                resetStatusBar(dialog);
            }
        }

        public void onClickSend(View view) {
            Log.d(TAG, "onClickSend() called with: view = [" + view + "]");

            if (getActivity() instanceof WindowControl) {
                // navigate to send screen

                ((WindowControl) getActivity()).getFragmentUtility().add(
                        SendCoinsFragment.newInstance(),
                        FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                        FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                        SendFragment.TAG
                );
            }
        }

        public void onClickTransaction(View view) {
            if (getActivity() instanceof WindowControl) {
                AccountHistoryResponseItem accountHistoryItem = (AccountHistoryResponseItem) view.getTag();
                if (accountHistoryItem != null) {
                    // show webview dialog
                    WebViewDialogFragment dialog = WebViewDialogFragment.newInstance(getString(R.string.home_explore_url, accountHistoryItem.getHash()), "");
                    dialog.show(((WindowControl) getActivity()).getFragmentUtility().getFragmentManager(),
                            WebViewDialogFragment.TAG);

                    resetStatusBar(dialog);
                }
            }
        }

        /**
         * Execute all pending transactions and set up a listener to set the status bar to
         * blue when the dialog is closed
         *
         * @param dialog Instance of the dialog to listen for closing on
         */
        private void resetStatusBar(BaseDialogFragment dialog) {
            // make sure that dialog is not null
            ((WindowControl) getActivity()).getFragmentUtility().getFragmentManager().executePendingTransactions();

            // reset status bar to blue when dialog is closed
            if (dialog != null && dialog.getDialog() != null) {
                dialog.getDialog().setOnDismissListener(dialogInterface -> setStatusBarBlue());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resumePendingTransactions(getActivity());
    }

    @Override
    public void showLoading() {
        mainThreadEnsurer.runOnMainThread(() -> {
            binding.homeSwiperefresh.setRefreshing(true);
        });
    }

    @Override
    public void hideLoading() {
        mainThreadEnsurer.runOnMainThread(() -> {
            binding.homeSwiperefresh.setRefreshing(false);
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (binding.homeSwiperefresh != null) {
            binding.homeSwiperefresh.setRefreshing(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }


}

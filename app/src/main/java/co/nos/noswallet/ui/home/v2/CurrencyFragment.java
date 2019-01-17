package co.nos.noswallet.ui.home.v2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import co.nos.noswallet.MainActivity;
import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.R;
import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.common.BaseFragment;
import co.nos.noswallet.ui.home.adapter.HistoryAdapter;
import co.nos.noswallet.ui.home.v2.transactionDetail.TransactionDetailFragment;
import co.nos.noswallet.util.SharedPreferencesUtil;

public class CurrencyFragment extends BaseFragment<MainActivity> implements HasCurrency, CurrencyView {

    //todo: support for DI
    public CurrencyPresenter currencyPresenter = new CurrencyPresenter(new SharedPreferencesUtil(NOSApplication.get()));

    public static String TAG = CurrencyFragment.class.getSimpleName();
    public static final String CRYPTO_CURRENCY = "CRYPTO_CURRENCY";
    public Runnable afterResumeAction;

    TextView home_cryptocurrency_balance;
    TextView history_empty_label;
    SwipeRefreshLayout home_swiperefresh;
    RecyclerView home_recyclerview;

    private HistoryAdapter transactionsAdapter;
    private CryptoCurrencyFormatter currencyFormatter;

    private CryptoCurrency cryptoCurrency;

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return HomeFragment
     */
    public static CurrencyFragment newInstance(CryptoCurrency cryptoCurrency) {
        Bundle args = new Bundle();
        CurrencyFragment fragment = new CurrencyFragment();
        args.putSerializable(CRYPTO_CURRENCY, cryptoCurrency);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.cryptoCurrency = getSerializableArgument(CRYPTO_CURRENCY);
            this.currencyFormatter = new CryptoCurrencyFormatter().useCurrency(cryptoCurrency);
            this.currencyPresenter.setCurrencyFormatter(currencyFormatter);
            this.transactionsAdapter = new HistoryAdapter(currencyFormatter);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle $) {
        View view = inflater.inflate(R.layout.fragment_currency_list, container, false);
        home_cryptocurrency_balance = view.findViewById(R.id.home_cryptocurrency_balance);
        home_recyclerview = view.findViewById(R.id.home_recyclerview);
        home_swiperefresh = view.findViewById(R.id.home_swiperefresh);
        history_empty_label = view.findViewById(R.id.history_empty_label);

        currencyPresenter.attachView(this);
        configureAdapter();

        return view;
    }

    @Override
    public CryptoCurrency getCurrency() {
        return cryptoCurrency;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getParent() == null) {
            return;
        }
        WebsocketMachine machine = getParent().getWebsocketMachine();
        if (machine != null) {
            currencyPresenter.resume(machine, cryptoCurrency);
        }

        home_swiperefresh.postDelayed(additionalRunnable = this::callRefreshFromNotification, 2500);
    }

    private Runnable additionalRunnable = this::callRefreshFromNotification;

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (home_swiperefresh != null) {
            home_swiperefresh.removeCallbacks(additionalRunnable);
        }
    }

    private void configureAdapter() {
        home_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        home_recyclerview.setAdapter(transactionsAdapter);
        transactionsAdapter.listener = currencyPresenter::onTransactionEntryClick;

        home_swiperefresh.setOnRefreshListener(this::onSwipeToRefreshCalled);
    }

    public void callRefreshFromNotification() {
        if (getParent() == null || home_swiperefresh == null) {
            return;
        }
        home_swiperefresh.setRefreshing(true);
        history_empty_label.setVisibility(View.GONE);
        getParent().getWebsocketMachine().requestAccountHistory(cryptoCurrency);
        getParent().getWebsocketMachine().requestAccountInfo(cryptoCurrency);
    }

    private void onSwipeToRefreshCalled() {
        if (getParent() == null) {
            return;
        }
        history_empty_label.setVisibility(View.GONE);
        getParent().getWebsocketMachine().requestAccountHistory(cryptoCurrency);
        getParent().getWebsocketMachine().requestAccountInfo(cryptoCurrency);
    }

    @Override
    public boolean isNotAttached() {
        return getActivity() == null;
    }

    @Override
    public void showHistory(ArrayList<AccountHistory> history) {
        home_swiperefresh.setRefreshing(false);
        if (transactionsAdapter != null) {
            transactionsAdapter.refresh(history);
        }
        history_empty_label.setVisibility(View.GONE);
    }

    @Override
    public void onBalanceFormattedReceived(String formatted) {
        home_cryptocurrency_balance.setText(formatted);
    }

    @Override
    public void showNewAccount() {
        showHistory(new ArrayList<>());
        history_empty_label.setText(getString(R.string.failed_to_receive_history_new_account));
        history_empty_label.setVisibility(View.VISIBLE);
    }

    @Override
    public void navigateToTransactionEntry(AccountHistory accountHistory) {
        TransactionDetailFragment.showFrom(cryptoCurrency,accountHistory,getActivity());
    }
}

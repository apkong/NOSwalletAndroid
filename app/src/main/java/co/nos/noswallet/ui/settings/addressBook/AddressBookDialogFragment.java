package co.nos.noswallet.ui.settings.addressBook;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseDialogFragment;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.ui.settings.SettingsDialogFragment;
import co.nos.noswallet.ui.settings.addressBook.addAddress.AddAddressDialogFragment;
import co.nos.noswallet.ui.settings.addressBook.addressDetail.AddressDetailDialogFragment;
import co.nos.noswallet.util.CanReceiveAddress;


/**
 * Settings main screen
 */
public class AddressBookDialogFragment extends BaseDialogFragment implements AddressBookView {

    public enum State {
        SELECT_ADDRESS,
        BROWSING
    }

    public static String VIEW_STATE = "VIEW_STATE";
    public static String TAG = AddressBookDialogFragment.class.getSimpleName();
    public static String CURRENCY = "CURRENCY";

    private State state = State.BROWSING;
    private CryptoCurrency cryptoCurrency = CryptoCurrency.NOLLAR;

    private SearchView searchView;
    private RecyclerView recyclerView;
    private TextView emptyLabel;
    private TextView addNewAddressButton;

    private Handler handler;

    private String optionalAddress = null;

    @Inject
    AddressBookPresenter presenter;

    @Inject
    AddressesAdapter addressesAdapter;

    public static AddressBookDialogFragment newInstance() {
        return newInstance(State.BROWSING, CryptoCurrency.NOLLAR);
    }

    public static AddressBookDialogFragment newInstance(State state,
                                                        CryptoCurrency currency) {
        Bundle args = new Bundle();
        AddressBookDialogFragment fragment = new AddressBookDialogFragment();
        args.putSerializable(VIEW_STATE, state);
        args.putSerializable(CURRENCY, currency);
        fragment.setArguments(args);
        return fragment;
    }

    public static void showFrom(FragmentActivity activity) {
        if (activity instanceof WindowControl) {
            AddressBookDialogFragment dialog = AddressBookDialogFragment.newInstance();
            dialog.show(((WindowControl) activity).getFragmentUtility().getFragmentManager(),
                    AddressBookDialogFragment.TAG);

            // make sure that dialog is not null
            ((WindowControl) activity).getFragmentUtility().getFragmentManager().executePendingTransactions();
        }
    }

    public static void showFrom(FragmentActivity activity,
                                State initialState,
                                CryptoCurrency cryptoCurrency) {
        if (activity instanceof WindowControl) {
            AddressBookDialogFragment dialog = AddressBookDialogFragment.newInstance(initialState, cryptoCurrency);
            dialog.show(((WindowControl) activity).getFragmentUtility().getFragmentManager(),
                    AddressBookDialogFragment.TAG);

            // make sure that dialog is not null
            ((WindowControl) activity).getFragmentUtility().getFragmentManager().executePendingTransactions();
//
//            dialog.getDialog().setOnDismissListener(_dialog -> {
//                if (consumer != null) {
//                    try {
//                        consumer.accept(dialog.optionalAddress);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);
        handler = new Handler(Looper.getMainLooper());
        this.state = getSerializableArgument(VIEW_STATE);
        this.cryptoCurrency = getSerializableArgument(CURRENCY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        View view = inflater.inflate(R.layout.fragment_addressbook, container, false);

        setStatusBarColor(R.color.colorAccent);

        setupToolbar(view);

        presenter.attachView(this);

        searchView = view.findViewById(R.id.addressbook_searchview);
        setupSearchview();
        recyclerView = view.findViewById(R.id.addressbook_recyclerview);
        setupRecyclerView();
        emptyLabel = view.findViewById(R.id.addressbook_empty);

        addNewAddressButton = view.findViewById(R.id.addressbook_add_new);
        addNewAddressButton.setOnClickListener(v -> {
            hideKeyboard();
            presenter.requestAddAddressScreen();
        });

        if (state == State.SELECT_ADDRESS) {
            addNewAddressButton.setVisibility(View.GONE);
        } else if (state == State.BROWSING) {
            addNewAddressButton.setVisibility(View.VISIBLE);
        }

        presenter.loadEntries();
        optionalAddress = null;

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(addressesAdapter);
        addressesAdapter.refreshCurrency(cryptoCurrency);
        if (state == State.BROWSING) {
            addressesAdapter.listener = presenter::onAddressEntryClick;
        } else if (state == State.SELECT_ADDRESS) {
            addressesAdapter.listener = presenter::onAddressEntryClickWhenSelecting;
        }
    }

    private void setupSearchview() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!presenter.canFilterAnything()) {
                    //with empty dataset, prevent filtering
                    return false;
                }
                String query = newText.trim();
                presenter.filterAddresses(query);
                return false;
            }
        });
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.dialog_appbar);
        if (toolbar != null) {
            final AddressBookDialogFragment window = this;
            TextView title = view.findViewById(R.id.dialog_toolbar_title);
            title.setText(R.string.search_address);
            toolbar.setNavigationOnClickListener(v1 -> window.dismiss());
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void showErrorMessage(int resId) {
        emptyLabel.setText(resId);
        emptyLabel.setVisibility(View.VISIBLE);
        addressesAdapter.refresh(Collections.emptyList());
    }

    @Override
    public void onReceivedAddresses(List<AddressBookEntry> entries) {
        emptyLabel.setVisibility(View.GONE);
        addressesAdapter.refresh(entries);
    }

    @Override
    public void clearSearchAndReceive(List<AddressBookEntry> entries) {
        searchView.setQuery("", false);
        onReceivedAddresses(entries);
    }

    @Override
    public void navigateToAddAddressScreen() {
        AddAddressDialogFragment.showFrom(addNewAddressButton, getActivity(), this::reload);
    }

    @Override
    public void navigateToAddressEntryDetail(AddressBookEntry addressBookEntry) {
        AddressDetailDialogFragment.showFrom(addressBookEntry, getActivity(), this::reload);
    }

    @Override
    public void navigateBackWithResult(AddressBookEntry addressBookEntry) {

        if (getActivity() instanceof CanReceiveAddress) {
            CanReceiveAddress canReceiveAddress = ((CanReceiveAddress) getActivity());

            String addressToDeliver = addressBookEntry.addressesMap.get(cryptoCurrency);

            if (TextUtils.isEmpty(addressToDeliver)) {

                String information = getString(R.string.selected_account_not_has_address_placeholder, cryptoCurrency.name());
                Snackbar.make(addNewAddressButton, information, Snackbar.LENGTH_LONG).show();
                return;
            }

            canReceiveAddress.receiveAddress(addressToDeliver);
        }

        dismiss();
    }

    private void reload() {
        presenter.loadEntries();
    }
}

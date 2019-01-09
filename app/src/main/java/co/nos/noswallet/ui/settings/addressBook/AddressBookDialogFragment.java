package co.nos.noswallet.ui.settings.addressBook;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseDialogFragment;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.ui.settings.SettingsDialogFragment;
import io.reactivex.disposables.SerialDisposable;


/**
 * Settings main screen
 */
public class AddressBookDialogFragment extends BaseDialogFragment implements AddressBookView {

    // public static final String CURRENCY = "CURRENCY";

    public static String TAG = AddressBookDialogFragment.class.getSimpleName();

    private SearchView searchView;
    private RecyclerView recyclerView;
    private TextView emptyLabel;

    private SerialDisposable serialDisposable = new SerialDisposable();

    private Handler handler;

    @Inject
    AddressBookPresenter presenter;

    @Inject
    AddressesAdapter addressesAdapter;

    public static AddressBookDialogFragment newInstance() {
        Bundle args = new Bundle();
        AddressBookDialogFragment fragment = new AddressBookDialogFragment();
        // args.putSerializable(CURRENCY, currency);
        fragment.setArguments(args);
        return fragment;
    }

    public static void showFrom(FragmentActivity activity) {
        if (activity instanceof WindowControl) {
            AddressBookDialogFragment dialog = AddressBookDialogFragment.newInstance();
            dialog.show(((WindowControl) activity).getFragmentUtility().getFragmentManager(),
                    SettingsDialogFragment.TAG);

            // make sure that dialog is not null
            ((WindowControl) activity).getFragmentUtility().getFragmentManager().executePendingTransactions();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);
        handler = new Handler(Looper.getMainLooper());
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

        TextView addNewAddress = view.findViewById(R.id.addressbook_add_new);
        addNewAddress.setOnClickListener(v -> {
            hideKeyboard();
            Toast.makeText(getActivity(), "Feature in development", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadEntries();
    }


    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(addressesAdapter);
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
}

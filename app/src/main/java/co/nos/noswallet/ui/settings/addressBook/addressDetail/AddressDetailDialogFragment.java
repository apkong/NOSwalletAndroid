package co.nos.noswallet.ui.settings.addressBook.addressDetail;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseDialogFragment;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.ui.settings.addressBook.AddressBookEntry;


public class AddressDetailDialogFragment extends BaseDialogFragment implements AddressDetailView {

    public static final String ENTRY = "ENTRY";

    public static String TAG = AddressDetailDialogFragment.class.getSimpleName();

    @Nullable
    private AddressBookEntry entry;

    //    private EditText nameInput;
    private EditText nollarInput, nosInput, bananoInput, nanoInput;

    private TextView errorLabel;

    private Handler handler;

    @Inject
    AddressDetailPresenter presenter;

    public static AddressDetailDialogFragment newInstance(AddressBookEntry entry) {
        Bundle args = new Bundle();
        AddressDetailDialogFragment fragment = new AddressDetailDialogFragment();
        args.putSerializable(ENTRY, entry);
        fragment.setArguments(args);
        return fragment;
    }

    public static void showFrom(AddressBookEntry entry, FragmentActivity activity, Runnable dismissAction) {
        openAddressDetailFragment(entry, activity, dismissAction);
    }

    private static void openAddressDetailFragment(AddressBookEntry entry,
                                                  FragmentActivity activity,
                                                  Runnable runnable) {
        if (activity instanceof WindowControl) {
            AddressDetailDialogFragment dialog = AddressDetailDialogFragment.newInstance(entry);
            dialog.show(((WindowControl) activity).getFragmentUtility().getFragmentManager(),
                    AddressDetailDialogFragment.TAG);

            // make sure that dialog is not null
            ((WindowControl) activity).getFragmentUtility().getFragmentManager().executePendingTransactions();

            if (dialog.getDialog() != null)
                dialog.getDialog().setOnDismissListener(dialog1 -> {
                    if (runnable != null) {
                        runnable.run();
                    }
                });
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);
        entry = getSerializableArgument(ENTRY);
        handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        View view = inflater.inflate(R.layout.fragment_address_entry_detail, container, false);

        setStatusBarColor(R.color.colorAccent);

        setupToolbar(view);

        presenter.attachView(this);

        nollarInput = view.findViewById(R.id.address_nollar_account_input);
        nosInput = view.findViewById(R.id.address_nos_account_input);
        bananoInput = view.findViewById(R.id.address_banano_account_input);
        nanoInput = view.findViewById(R.id.address_nano_account_input);

        if (entry != null && entry.addressesMap != null && !entry.addressesMap.isEmpty()) {
            Map<CryptoCurrency, String> map = entry.addressesMap;
            String nollar = map.get(CryptoCurrency.NOLLAR);
            String nos = map.get(CryptoCurrency.NOS);
            String banano = map.get(CryptoCurrency.BANANO);
            String nano = map.get(CryptoCurrency.NANO);
            if (nollar != null) {
                nollarInput.setText(nollar);
            } else {
                String fixedAddress = presenter.resolveFrom(map, CryptoCurrency.NOLLAR);
                nollarInput.setText(fixedAddress);
            }
            if (nos != null) {
                nosInput.setText(nos);
            } else {
                String fixedAddress = presenter.resolveFrom(map, CryptoCurrency.NOS);
                nosInput.setText(fixedAddress);
            }
            if (banano != null) {
                bananoInput.setText(banano);
            } else {
                String fixedAddress = presenter.resolveFrom(map, CryptoCurrency.BANANO);
                bananoInput.setText(fixedAddress);
            }
            if (nano != null) {
                nanoInput.setText(nano);
            } else {
                String fixedAddress = presenter.resolveFrom(map, CryptoCurrency.NANO);
                nanoInput.setText(fixedAddress);
            }
        }

//        nameInput = view.findViewById(R.id.address_name_input);
        errorLabel = view.findViewById(R.id.address_label_error);

        TextView deleteButton = view.findViewById(R.id.address_delete);
        deleteButton.setOnClickListener(v -> {
            presenter.delete(entry);
        });
        TextView saveButton = view.findViewById(R.id.address_save);
        saveButton.setOnClickListener(v -> {
            hideKeyboard();
            String nollarAddress = nollarInput.getText().toString().trim();
            String nosAddress = nosInput.getText().toString().trim();
            String bananoAddress = bananoInput.getText().toString().trim();
            String nanoAddress = nanoInput.getText().toString().trim();

            presenter.save(entry, nollarAddress, nosAddress, bananoAddress, nanoAddress);

        });

        return view;
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.dialog_appbar);
        if (toolbar != null) {
            final AddressDetailDialogFragment window = this;
            TextView title = view.findViewById(R.id.dialog_toolbar_title);
            title.setText(entry == null ? "untitled" : entry.name);
            toolbar.setNavigationOnClickListener(v1 -> {
                window.dismiss();
            });
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void showErrorMessage(int resId) {
        errorLabel.setVisibility(View.VISIBLE);
        if (getContext() != null) {
            errorLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
        errorLabel.setText(resId);
    }

    @Override
    public void showErrorMessage(int resId, CryptoCurrency currency) {
        errorLabel.setVisibility(View.VISIBLE);
        if (getContext() != null) {
            errorLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
        errorLabel.setText(getString(resId, currency.name()));
    }

    @Override
    public void onAddressSaved() {
        errorLabel.setText(getString(R.string.address_added));
        errorLabel.setVisibility(View.VISIBLE);
        if (getContext() != null) {
            errorLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        }
        handler.postDelayed(this::dismiss, 400);
    }

    @Override
    public void onAddressDeleted() {
        errorLabel.setText(getString(R.string.address_deleted));
        errorLabel.setVisibility(View.VISIBLE);
        if (getContext() != null) {
            errorLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
        handler.postDelayed(this::dismiss, 400);
    }

    @Override
    public void clearErrors() {
        errorLabel.setText("");
    }
}

package co.nos.noswallet.ui.settings.addressBook.addAddress;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseDialogFragment;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.ui.settings.SettingsDialogFragment;
import co.nos.noswallet.ui.settings.addressBook.AddressBookEntry;

public class AddAddressDialogFragment extends BaseDialogFragment implements AddAddressView {

    public static final String CURRENCY = "CURRENCY";

    public static String TAG = AddAddressDialogFragment.class.getSimpleName();

    @Nullable
    private CryptoCurrency cryptoCurrency = CryptoCurrency.NOLLAR;

    private EditText nameInput, accountInput;

    private TextView errorLabel;

    private Handler handler;

    @Inject
    AddAddressPresenter presenter;

    public static AddAddressDialogFragment newInstance(CryptoCurrency currency) {
        Bundle args = new Bundle();
        AddAddressDialogFragment fragment = new AddAddressDialogFragment();
        args.putSerializable(CURRENCY, currency);
        fragment.setArguments(args);
        return fragment;
    }

    public static void showFrom(View view, FragmentActivity activity, Runnable dismissAction) {
        PopupMenu popup = new PopupMenu(activity, view);
        Menu m = popup.getMenu();
        for (CryptoCurrency cryptoCurrency : CryptoCurrency.values()) {
            m.add(cryptoCurrency.name());
        }
        popup.setOnMenuItemClickListener(item -> {
            String currencyName = String.valueOf(item.getTitle());
            CryptoCurrency cryptoCurrency = CryptoCurrency.recognize(currencyName);

            openAddAddressFragment(cryptoCurrency, activity, dismissAction);

            return true;
        });

        popup.show();
    }

    private static void openAddAddressFragment(CryptoCurrency cryptoCurrency,
                                               FragmentActivity activity,
                                               Runnable runnable) {
        if (activity instanceof WindowControl) {
            AddAddressDialogFragment dialog = AddAddressDialogFragment.newInstance(cryptoCurrency);
            dialog.show(((WindowControl) activity).getFragmentUtility().getFragmentManager(),
                    SettingsDialogFragment.TAG);

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
        cryptoCurrency = getSerializableArgument(CURRENCY);
        handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        View view = inflater.inflate(R.layout.fragment_add_address_entry, container, false);

        setStatusBarColor(R.color.colorAccent);

        setupToolbar(view);

        presenter.attachView(this);

        TextView account_label = view.findViewById(R.id.address_label);
        if (cryptoCurrency != null) {
            account_label.setText(String.format("%s address", cryptoCurrency.name()));
        }
        nameInput = view.findViewById(R.id.address_name_input);
        accountInput = view.findViewById(R.id.address_account_input);
        errorLabel = view.findViewById(R.id.address_label_error);

        TextView saveButton = view.findViewById(R.id.address_save);
        saveButton.setOnClickListener(v -> {
            hideKeyboard();
            String name = nameInput.getText().toString().trim();
            String address = accountInput.getText().toString().trim();

            if (!name.isEmpty() && !address.isEmpty()) {
                presenter.addAddress(cryptoCurrency, name, address);
            }
        });

        return view;
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.dialog_appbar);
        if (toolbar != null) {
            final AddAddressDialogFragment window = this;
            TextView title = view.findViewById(R.id.dialog_toolbar_title);
            title.setText(R.string.add_address);
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
        showErrorMessage(getString(resId));
    }

    @Override
    public void showErrorMessage(int resId, Object... args) {
        showErrorMessage(getString(resId, args));
    }

    @Override
    public void onAddressSaved(AddressBookEntry entry) {
        errorLabel.setText(getString(R.string.address_added));
        errorLabel.setVisibility(View.VISIBLE);
        if (getContext() != null) {
            errorLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        }
        handler.postDelayed(this::dismiss, 400);
    }

    @Override
    public void clearErrors() {
        errorLabel.setText("");
    }

    private void showErrorMessage(String message) {
        errorLabel.setVisibility(View.VISIBLE);
        if (getContext() != null) {
            errorLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
        errorLabel.setText(message);
    }
}

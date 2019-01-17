package co.nos.noswallet.ui.settings.setRepresentative;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
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


/**
 * Settings main screen
 */
public class SetRepresentativeDialogFragment extends BaseDialogFragment implements SetRepresentativeView {

    public static final String CURRENCY = "CURRENCY";

    public static String TAG = SetRepresentativeDialogFragment.class.getSimpleName();

    @Nullable
    private CryptoCurrency cryptoCurrency;

    private EditText representativeInput;

    private TextView errorLabel;

    private Handler handler;

    @Inject
    SetRepresentativePresenter presenter;

    public static SetRepresentativeDialogFragment newInstance(CryptoCurrency currency) {
        Bundle args = new Bundle();
        SetRepresentativeDialogFragment fragment = new SetRepresentativeDialogFragment();
        args.putSerializable(CURRENCY, currency);
        fragment.setArguments(args);
        return fragment;
    }

    public static void showFrom(View view, FragmentActivity activity) {
        PopupMenu popup = new PopupMenu(activity, view);
        Menu m = popup.getMenu();
        for (CryptoCurrency cryptoCurrency : CryptoCurrency.values()) {
            m.add(cryptoCurrency.name());
        }
        popup.setOnMenuItemClickListener(item -> {
            String currencyName = String.valueOf(item.getTitle());
            CryptoCurrency cryptoCurrency = CryptoCurrency.recognize(currencyName);

            openRepresentativeFragment(cryptoCurrency, activity);

            return true;
        });

        popup.show();
    }

    private static void openRepresentativeFragment(CryptoCurrency cryptoCurrency, FragmentActivity activity) {
        if (activity instanceof WindowControl) {
            SetRepresentativeDialogFragment dialog = SetRepresentativeDialogFragment.newInstance(cryptoCurrency);
            dialog.show(((WindowControl) activity).getFragmentUtility().getFragmentManager(),
                    SetRepresentativeDialogFragment.TAG);

            // make sure that dialog is not null
            ((WindowControl) activity).getFragmentUtility().getFragmentManager().executePendingTransactions();
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

        View view = inflater.inflate(R.layout.fragment_change_representative, container, false);

        setStatusBarColor(R.color.colorAccent);

        setupToolbar(view);

        presenter.attachView(this);

        representativeInput = view.findViewById(R.id.representative_input);
        errorLabel = view.findViewById(R.id.representative_label_error);
        TextView saveButton = view.findViewById(R.id.representative_save);
        saveButton.setOnClickListener(v -> {
            hideKeyboard();
            String text = representativeInput.getText().toString().trim();
            presenter.saveRepresentativeClicked(cryptoCurrency, text);
        });
        presenter.requestCachedRepresentative(cryptoCurrency);
        return view;
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.dialog_appbar);
        if (toolbar != null) {
            final SetRepresentativeDialogFragment window = this;
            TextView title = view.findViewById(R.id.dialog_toolbar_title);
            title.setText(R.string.change_representative);
            toolbar.setNavigationOnClickListener(v1 -> window.dismiss());
        }
    }

    @Override
    public void clearRepresentativeError() {
        errorLabel.setText("");
    }

    @Override
    public void showRepresentativeError(int stringRes) {
        errorLabel.setTextColor(getResources().getColor(R.color.red));
        errorLabel.setText(stringRes);
    }

    @Override
    public void showRepresentativeSavedAndExit(int stringRes) {
        errorLabel.setTextColor(getResources().getColor(R.color.green));
        errorLabel.setText(stringRes);
        handler.postDelayed(this::dismiss, 400);
    }

    @Override
    public void onRepresentativeReceived(String previousRepresentative) {
        representativeInput.setText(previousRepresentative);
        representativeInput.setSelection(previousRepresentative.length());
    }

    @Override
    public void dismiss() {
        super.dismiss();
        handler.removeCallbacksAndMessages(null);
    }
}

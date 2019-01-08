package co.nos.noswallet.ui.settings.setRepresentative;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import co.nos.noswallet.R;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.common.BaseDialogFragment;


/**
 * Settings main screen
 */
public class SetRepresentativeDialogFragment extends BaseDialogFragment {

    public static final String CURRENCY = "CURRENCY";
    public static final String REPRESENTATIVE = "REPRESENTATIVE";

    public static String TAG = SetRepresentativeDialogFragment.class.getSimpleName();

    @Nullable
    private CryptoCurrency cryptoCurrency;

    @Nullable
    private String representative;

    private EditText representativeInput;

    private TextView saveButton, errorLabel;


    /**
     * Create new instance of the dialog fragment (handy pattern if any data needs to be passed to it)
     *
     * @return New instance of SettingsDialogFragment
     */
    public static SetRepresentativeDialogFragment newInstance(CryptoCurrency currency, String existingRepresentative) {
        Bundle args = new Bundle();
        SetRepresentativeDialogFragment fragment = new SetRepresentativeDialogFragment();
        args.putSerializable(CURRENCY, currency);
        args.putString(REPRESENTATIVE, existingRepresentative);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);
        cryptoCurrency = getSerializableArgument(CURRENCY);
        if (getArguments() != null) {
            representative = getArguments().getString(REPRESENTATIVE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_representative, container, false);

        setStatusBarColor(R.color.colorAccent);

        setupToolbar(view);

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

}

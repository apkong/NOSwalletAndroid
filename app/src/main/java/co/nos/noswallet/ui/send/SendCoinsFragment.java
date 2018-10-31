package co.nos.noswallet.ui.send;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.Reprint;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

import javax.inject.Inject;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.R;
import co.nos.noswallet.databinding.FragmentSendCoinsBinding;
import co.nos.noswallet.model.Address;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseFragment;
import co.nos.noswallet.ui.common.KeyboardUtil;
import co.nos.noswallet.ui.common.UIUtil;
import co.nos.noswallet.ui.scan.ScanActivity;

import static android.app.Activity.RESULT_OK;

/**
 * Send Screen
 */
public class SendCoinsFragment extends BaseFragment implements SendCoinsView {
    private FragmentSendCoinsBinding binding;
    public static String TAG = SendCoinsFragment.class.getSimpleName();

    private AlertDialog fingerprintDialog;
    private static final String ARG_NEW_SEED = "argNewSeed";
    private String newSeed;

    private Button chooseCurrencyButton;

    ClickHandlers ClickHandlers;

    @Inject
    SendCoinsPresenter presenter;

    @BindingAdapter("layout_constraintGuide_percent")
    public static void setLayoutConstraintGuidePercent(Guideline guideline, float percent) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        params.guidePercent = percent;
        guideline.setLayoutParams(params);
    }

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return New instance of SendFragment
     */
    public static SendCoinsFragment newInstance() {
        Bundle args = new Bundle();
        SendCoinsFragment fragment = new SendCoinsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return New instance of SendFragment
     */
    public static SendCoinsFragment newInstance(String newSeed) {
        Bundle args = new Bundle();
        args.putString(ARG_NEW_SEED, newSeed);
        SendCoinsFragment fragment = new SendCoinsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            newSeed = getArguments().getString(ARG_NEW_SEED);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_send, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.send_camera:
                startScanActivity(getString(R.string.scan_send_instruction_label), false);
                return true;
        }

        return false;
    }

    public void showSoftKeyboard() {
        //Shows the SoftKeyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && getActivity().getCurrentFocus() != null) {
            inputMethodManager.showSoftInput(binding.sendAddress, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        // change keyboard mode
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        KeyboardUtil.hideKeyboard(getActivity());

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_send_coins, container, false);
        view = binding.getRoot();

        chooseCurrencyButton = view.findViewById(R.id.choose_currency_button);

        binding.setHandlers(ClickHandlers = new ClickHandlers());

        chooseCurrencyButton.setOnClickListener(ClickHandlers::onClickChangeCurrency);

        setStatusBarBlue();
        setBackEnabled(true);
        setTitle(getString(R.string.send_title));
        //setTitleDrawable(R.drawable.ic_send);

        // hide keyboard for edittext fields
        binding.sendAmountNano.setInputType(InputType.TYPE_NULL);
        binding.sendAmountLocalcurrency.setVisibility(View.GONE);
        binding.sendAmountLocalcurrency.setInputType(InputType.TYPE_NULL);

        // set active and inactive states for edittext fields
        binding.sendAmountNano.setOnFocusChangeListener((view1, b) -> toggleFieldFocus((EditText) view1, b, false));
        binding.sendAmountLocalcurrency.setOnFocusChangeListener((view1, b) -> toggleFieldFocus((EditText) view1, b, true));
        binding.sendAmountLocalcurrency.setHint(NumberFormat.getCurrencyInstance(Locale.ENGLISH).format(0));
        binding.setShowAmount(true);

        binding.sendAddress.setOnFocusChangeListener((view12, hasFocus) -> binding.setShowAmount(!hasFocus));

        binding.sendAddress.setBackgroundResource(binding.sendAddress.getText().length() > 0 ? R.drawable.bg_seed_input_active : R.drawable.bg_seed_input);
        UIUtil.colorizeSpannable(binding.sendAddress.getText(), getContext());


        // updates to handle seed conversion 1.0.2
        if (newSeed != null) {
            String address = NOSUtil.publicToAddress(NOSUtil.privateToPublic(NOSUtil.seedToPrivate(newSeed)));
            binding.sendAddress.setText(address);
            setShortAddress();
        }

        presenter.attachView(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unregister from bus
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check to make sure we are responding to camera result
        if (requestCode == SCAN_RESULT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Bundle res = data.getExtras();
                if (res != null) {
                    // parse address
                    Address address = new Address(res.getString(ScanActivity.QR_CODE_RESULT), presenter.currencyInUse);

                    // set to scanned value
                    if (address.getAddress() != null) {
                        presenter.setTargetAddress(address.getAddress());
                        binding.sendAddress.setText(address.getAddress());
                    }
                    setShortAddress();
                }
            }
        }
    }

    @Override
    public void showError(String message) {
        showError(R.string.send_error_alert_title, message);
    }

    public void showError(String message, boolean exit) {
        showError(R.string.send_error_alert_title, message, exit);
    }

    private boolean validateAddress() {
        // check for valid address
        Address destination = new Address(binding.sendAddress.getText().toString(), presenter.currencyInUse);
        if (!destination.isValidAddress()) {
            showError(R.string.send_error_alert_title, R.string.send_error_alert_message);
            return false;
        }
        return true;
    }

    private String getCurrentTypedCoins() {
        return binding.sendAmountNano.getText().toString().trim();
    }

    private void setCurrentTypedCoins(String value) {
        binding.sendAmountNano.setText(value);
    }

    private void enableSendIfPossible() {
        boolean enableSend = presenter.canTransferNeuros(getCurrentTypedCoins());
        binding.sendSendButton.setBackgroundResource(enableSend ? R.drawable.bg_large_button : R.drawable.bg_large_button_gray);
        binding.sendSendButton.setEnabled(enableSend);
    }

    public void showError(int title, String message) {
        showError(title, message, true);
    }

    public void showError(int title, String message, boolean exit) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.send_amount_too_large_alert_cta, (dialog, which) -> {
                    if (exit) {
                        exitThisScreen();
                    }
                })
                .show();
    }

    @Override
    public void showError(int title, int message) {
        showError(title, getString(message));
    }

    @Override
    public void showAmountSent(String sendAmount, String targetAddress, CryptoCurrency cryptoCurrency) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setTitle(R.string.transfer_succeess)
                .setMessage(getString(R.string.sent_coins_amount_to_address_currency_placeholder, sendAmount, cryptoCurrency.name(), targetAddress))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    exitThisScreen();
                })
                .show();
    }

    @Override
    public void onNewCurrencyReceived(CryptoCurrency currencyInUse) {
        chooseCurrencyButton.setText(currencyInUse.name());
    }

    private void exitThisScreen() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    /**
     * Helper to set focus size and color on fields
     *
     * @param v               EditText view
     * @param hasFocus        Does view have focus currently?
     * @param isLocalCurrency Is this view the local currency view?
     */
    private void toggleFieldFocus(EditText v, boolean hasFocus, boolean isLocalCurrency) {

        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, hasFocus ? 20f : 16f);
        binding.sendAmountNanoSymbol.setAlpha(hasFocus && !isLocalCurrency ? 1.0f : 0.5f);

        // clear amounts
        setCurrentTypedCoins("");

        // set local currency decimal separator if local currency is active, otherwise . for nano
        //binding.sendKeyboardDecimal.setText(localCurrencyActive ? wallet.getDecimalSeparator() : ".");
    }

    /**
     * Update amount strings based on input processed
     *
     * @param value String value of character pressed
     */
    private void updateAmount(CharSequence value) {
        Log.d(TAG, "updateAmount() called with: value = [" + value + "]");

        presenter.updateAmount(value);
        enableSendIfPossible();
    }

    private void setShortAddress() {
        // set short address if appropriate
        Address address = new Address(binding.sendAddress.getText().toString(), presenter.currencyInUse);
        if (address.isValidAddress()) {
            binding.sendAddressDisplay.setText(address.getColorizedShortSpannable());
            binding.sendAddressDisplay.setBackgroundResource(binding.sendAddressDisplay.length() > 0 ? R.drawable.bg_seed_input_active : R.drawable.bg_seed_input);
        } else {
            binding.sendAddressDisplay.setText("");
            binding.sendAddressDisplay.setBackgroundResource(binding.sendAddressDisplay.length() > 0 ? R.drawable.bg_seed_input_active : R.drawable.bg_seed_input);
            showError(getString(R.string.error_qr_recognizing, presenter.currencyInUse.name()), false);
        }
        enableSendIfPossible();
    }

    @Override
    public void onCurrentInputReceived(String currentInput) {
        setCurrentTypedCoins(currentInput);
    }

    public class ClickHandlers {
        /**
         * Listener for styling updates when text changes
         *
         * @param s      Character sequence
         * @param start  Starting character
         * @param before Character that came before
         * @param count  Total character count
         */
        public void onAddressTextChanged(CharSequence s, int start, int before, int count) {
            // set background to active or not
            binding.sendAddress.setBackgroundResource(s.length() > 0 ? R.drawable.bg_seed_input_active : R.drawable.bg_seed_input);

            // colorize input string
            UIUtil.colorizeSpannable(binding.sendAddress.getText(), getContext());
        }

        public void onAddressDisplayClicked(View view) {
            binding.setShowAmount(false);
            binding.sendAddress.setSelection(binding.sendAddress.getText().length());
            showSoftKeyboard();
        }

        public void onClickNanoContainer(View view) {
            binding.sendAmountNano.requestFocus();
        }

        public void onClickConfirm(View view) {
            presenter.setTargetAddress(binding.sendAddress.getText().toString().trim());
            binding.setShowAmount(true);
            setShortAddress();
            KeyboardUtil.hideKeyboard(getActivity());
            binding.sendAmountNano.requestFocus();
        }

        @SuppressLint("CheckResult")
        public void onClickSend(View view) {
            System.out.println("onClickSend()");
            if (!validateAddress()) {
                System.out.println("address invalid");
                return;
            }
            Credentials credentials = presenter.provideCredentials();

            if (Reprint.isHardwarePresent() && Reprint.hasFingerprintRegistered()) {
                // show fingerprint dialog
                LayoutInflater factory = LayoutInflater.from(getContext());
                @SuppressLint("InflateParams")
                final View viewFingerprint = factory.inflate(R.layout.view_fingerprint, null);
                showFingerprintDialog(viewFingerprint);
                com.github.ajalt.reprint.rxjava2.RxReprint.authenticate()
                        .subscribe(result -> {
                            switch (result.status) {
                                case SUCCESS:
                                    showFingerprintSuccess(viewFingerprint);
                                    break;
                                case NONFATAL_FAILURE:
                                    showFingerprintError(result.failureReason, result.errorMessage, viewFingerprint);
                                    break;
                                case FATAL_FAILURE:
                                    showFingerprintError(result.failureReason, result.errorMessage, viewFingerprint);
                                    break;
                            }
                        });
            } else if (credentials != null && credentials.getPin() != null) {
                showPinScreen(getString(R.string.send_pin_description, getCurrentTypedCoins()), () -> presenter.attemptSendCoins(getCurrentTypedCoins()));
            } else if (credentials != null && credentials.getPin() == null) {
                showCreatePinScreen();
            }
        }

        public void onClickMax(View view) {
            //analyticsService.track(AnalyticsEvents.SEND_MAX_AMOUNT_USED);
            String maxNeuros = presenter.getTotalNeurosAmount();
            presenter.updateAmountFromCode(maxNeuros);
            enableSendIfPossible();
        }

        public void onClickChangeCurrency(View view) {
            Log.e(TAG, "onClickChangeCurrency: " + view);
            if (view instanceof Button) {
                String text = ((Button) view).getText().toString();
                presenter.changeCurrency(text);

            }
        }

        public void onClickNumKeyboard(View view) {
            updateAmount(((Button) view).getText());
        }
    }

    private void showFingerprintDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.send_fingerprint_title));
        builder.setMessage(getString(R.string.send_fingerprint_description,
                !getCurrentTypedCoins().isEmpty() ? getCurrentTypedCoins() : "0"));
        builder.setView(view);
        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText, (dialog, which) -> Reprint.cancelAuthentication());

        fingerprintDialog = builder.create();
        fingerprintDialog.setCanceledOnTouchOutside(false);
        // display dialog
        fingerprintDialog.show();
    }

    private void showFingerprintSuccess(View view) {
        if (isAdded()) {
            TextView textView = view.findViewById(R.id.fingerprint_textview);
            textView.setText(getString(R.string.send_fingerprint_success));
            if (getContext() != null) {
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_sky_blue));
            }
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fingerprint_success, 0, 0, 0);

            presenter.attemptSendCoins(getCurrentTypedCoins());

            // close dialog after 1 second
            final Handler handler = new Handler();
            final Runnable runnable = () -> {
                if (fingerprintDialog != null && fingerprintDialog.isShowing()) {
                    fingerprintDialog.dismiss();
                }
            };
            handler.postDelayed(runnable, 500);
        }
    }

    private void showFingerprintError(AuthenticationFailureReason reason, CharSequence message, View view) {
        if (isAdded()) {
            final HashMap<String, String> customData = new HashMap<>();
            customData.put("description", reason.name());
            //analyticsService.track(AnalyticsEvents.SEND_AUTH_ERROR, customData);
            TextView textView = view.findViewById(R.id.fingerprint_textview);
            textView.setText(message.toString());
            if (getContext() != null) {
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.error));
            }
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fingerprint_error, 0, 0, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        WebsocketMachine machine = WebsocketMachine.obtain(getActivity());
        if (machine != null) {
            machine.pausePendingTransactions();
        }
        presenter.observeWebsocketMachine(machine);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.cancelWebsocketObservation();
    }

    @Override
    public void showLoading() {
        binding.getRoot().findViewById(R.id.fragment_send_overlay).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.getRoot().findViewById(R.id.fragment_send_overlay).setVisibility(View.GONE);
    }
}

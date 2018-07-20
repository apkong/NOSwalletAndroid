package co.nos.noswallet.kyc.phoneNumber;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import br.com.sapereaude.maskedEditText.MaskedEditText;
import co.nos.noswallet.R;
import co.nos.noswallet.ui.common.InputLayout;

public class PrefixedPhoneInputLayout extends RelativeLayout {

    MaskedEditText phonePrefixInput;

    TextInputLayout phonePrefixInputLayout;

    InputLayout phoneInput;

    ImageView dropdownImageView;

    private CountryPickerListener countryPickerListener;

    public PrefixedPhoneInputLayout(Context context) {
        super(context);
        setup(context, null);
    }

    public PrefixedPhoneInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public PrefixedPhoneInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs);
    }

    public void setCountryPickerListener(CountryPickerListener countryPickerListener) {
        this.countryPickerListener = countryPickerListener;
    }

    private void setup(Context context, AttributeSet attrs) {
        inflate(context, R.layout.item_prefixed_phone_input, this);
        injectViews();
        configurePrefixInput();
    }

    private void injectViews() {
        phonePrefixInput = findViewById(R.id.item_create_account_phone_masked_prefix);
        phonePrefixInputLayout = findViewById(R.id.item_create_account_phone_prefix);
        phoneInput = findViewById(R.id.item_create_account_phone);
        dropdownImageView = findViewById(R.id.create_account_pick_prefix_dropdown);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configurePrefixInput() {
        phonePrefixInput.setOnTouchListener((v, event) -> {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                if (countryPickerListener != null) {
                    countryPickerListener.requestCountryPicker();
                }
            }
            v.clearFocus();
            return true;
        });

        dropdownImageView.setOnClickListener(v -> {
            if (countryPickerListener != null) {
                countryPickerListener.requestCountryPicker();
            }
        });
    }

    public void setOnActionDoneListener(@Nullable OnClickListener listener) {
        phonePrefixInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        phonePrefixInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (listener != null) {
                    listener.onClick(v);
                }
                return true;
            }
            return false;
        });
    }

    public MaskedEditText getPhonePrefixInput() {
        return phonePrefixInput;
    }

    public InputLayout getPhoneInput() {
        return phoneInput;
    }

    public void setError(String message) {
        phoneInput.setError(message);
    }

    public void setDialCode(String dialCode) {
        phonePrefixInput.setText(dialCode);
        phoneInput.setSelection(phoneInput.length());
        phoneInput.requestFocus();
    }

    public void clearError() {
        phoneInput.clearError();
    }

    public interface CountryPickerListener {
        void requestCountryPicker();
    }
}

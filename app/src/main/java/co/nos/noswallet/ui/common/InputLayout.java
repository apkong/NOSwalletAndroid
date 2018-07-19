package co.nos.noswallet.ui.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;


import co.nos.noswallet.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class InputLayout extends TextInputLayout {

    TextInputEditText editText;

    public InputLayout(Context context) {
        super(context);
        setup(context, null);
    }

    public InputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public InputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
        editText = new TextInputEditText(context);
        setupEditText();
        setupAttributes(context, attrs);
    }

    private void setupEditText() {
        LayoutParams params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        editText.setLayoutParams(params);
        addView(editText);
    }

    private void setupAttributes(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.InputLayout);

        int inputType = array.getInt(R.styleable.InputLayout_android_inputType, InputType.TYPE_TEXT_VARIATION_NORMAL);
        setInputType(inputType);

        boolean passwordToggle = array.getBoolean(R.styleable.InputLayout_passwordToggleEnabled, false);
        setPasswordVisibilityToggleEnabled(passwordToggle);

        ColorStateList passwordToggleTint = array.getColorStateList(R.styleable.InputLayout_passwordToggleTint);
        setPasswordVisibilityToggleTintList(passwordToggleTint);

        int gravity = array.getInt(R.styleable.InputLayout_android_gravity, Gravity.START);
        setEditTextGravity(gravity);

        float textSize = array.getDimensionPixelSize(R.styleable.InputLayout_android_textSize, 60);
        setEditTextSize(textSize);

        setDefaultInputLayoutMargins();

        array.recycle();
    }

    private void setEditTextSize(final float textSizeInPixels) {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels);
    }

    private void setDefaultInputLayoutMargins() {
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(0, 0, 0, 0);
        setLayoutParams(layoutParams);

        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            View view = getChildAt(childIndex);
            view.setLayoutParams(layoutParams);
        }
    }

    public void setEditTextGravity(int gravity) {
        editText.setGravity(gravity);
    }

    @NonNull
    public String getText() {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    public void setText(CharSequence text) {
        editText.setText(text);
    }

    @Override
    public void setError(CharSequence error) {
        super.setErrorEnabled(true);
        super.setError(error);
    }

    public void clearError() {
        super.setErrorEnabled(false);
    }

    public void setInputType(int inputType) {
        editText.setInputType(inputType);
    }

    public void setPasswordVisibilityToggleEnabled(boolean passwordToggle) {
        super.setPasswordVisibilityToggleEnabled(passwordToggle);
    }

    public void setPasswordVisibilityToggleTintList(ColorStateList passwordToggleTint) {
        super.setPasswordVisibilityToggleTintList(passwordToggleTint);
    }

    public void setOnActionDoneListener(@Nullable final OnClickListener listener) {
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (listener != null) {
                    listener.onClick(v);
                }
                return true;
            }
            return false;
        });
    }

    public void blockLongClick() {
        if (getEditText() != null) {
            getEditText().setOnLongClickListener(v -> true);
        }
    }

    public void addTextChangedListener(TextWatcher watcher) {
        editText.addTextChangedListener(watcher);
    }

    public void removeTextChangedListener(TextWatcher watcher) {
        editText.removeTextChangedListener(watcher);
    }

    public void setSelection(int index) {
        editText.setSelection(index);
    }

    public int length() {
        return editText.length();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setOnEditTextTouchListener(OnTouchListener listener) {
        editText.setOnTouchListener(listener);
    }
}

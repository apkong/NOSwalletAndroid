package co.nos.noswallet.base;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;

public class NosAddressEditText extends android.support.v7.widget.AppCompatEditText {

    public TextWatcher textWatcher;

    public NosAddressEditText(Context context) {
        super(context);
        init(context, null);
    }

    public NosAddressEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NosAddressEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        super.addTextChangedListener(watcher);
        textWatcher = watcher;
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        super.removeTextChangedListener(watcher);
        textWatcher = null;
    }

    public void addTextChangedListener() {
        super.addTextChangedListener(textWatcher);
    }

    public void removeTextChangedListener() {
        super.removeTextChangedListener(textWatcher);
    }


    public boolean isNanoAddress() {
        String address = String.valueOf(getText()).trim();
        return address.startsWith("nano_");
    }

    public CharSequence handleNanoSeed() {
        String address = String.valueOf(getText()).trim();
        removeTextChangedListener();
        CharSequence newText = address.replace("nano_", "xrb_");
        setText(newText);
        addTextChangedListener();
        return newText;
    }
}

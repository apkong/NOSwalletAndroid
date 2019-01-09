package co.nos.noswallet.network.websockets.currencyFormatter;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;

import javax.inject.Inject;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.push.HandlePushMessagesService;

public class CryptoCurrencyFormatter {

    private CryptoCurrency cryptoCurrency;


    @Inject
    public CryptoCurrencyFormatter() {
    }

    public CryptoCurrencyFormatter(CryptoCurrency cryptoCurrency) {
        this.cryptoCurrency = cryptoCurrency;
    }

    public CryptoCurrencyFormatter useCurrency(CryptoCurrency cryptoCurrency) {
        this.cryptoCurrency = cryptoCurrency;
        return this;
    }

    /**
     * formats uiValue to raw. (20_000 raw stands for 200.00)
     *
     * @param uiValue
     * @return
     */
    public String uiToRaw(String uiValue) {
        return cryptoCurrency.uiToRaw(uiValue);
    }

    public String rawtoUi(String raw) {
        return cutOutTrailingZeros(cryptoCurrency.rawToUi(raw));
    }

    public String cutOutTrailingZeros(String expression) {
        return HandlePushMessagesService.formatWell(expression);
    }

    public SpannableString createSpannable(String account_number) {
        String bluePath = account_number.substring(0, 8);
        int len = account_number.length();
        String orangePath = account_number.substring(len - 6, len);

        String totalPath = bluePath + orangePath;

        SpannableString spannableString = new SpannableString(totalPath);

        ColorStateList blue = new ColorStateList(new int[][]{new int[]{}}, new int[]{0xff4a90e2});
        ColorStateList orange = new ColorStateList(new int[][]{new int[]{}}, new int[]{0xffea6232});

        TextAppearanceSpan blueSpan = colorSpan(blue);
        TextAppearanceSpan orangeSpan = colorSpan(orange);

        spannableString.setSpan(blueSpan, 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(orangeSpan, 8, totalPath.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    TextAppearanceSpan colorSpan(ColorStateList color) {
        TextAppearanceSpan span = new TextAppearanceSpan(null, Typeface.NORMAL, -1, color, null);
        return span;
    }
}

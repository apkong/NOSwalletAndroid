package co.nos.noswallet.model;

import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;

import java.io.Serializable;
import java.math.BigDecimal;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.persistance.currency.CryptoCurrency;

/**
 * Address class
 */

public class Address implements Serializable {

    public static final String TAG = Address.class.getSimpleName();

    private final CryptoCurrency cryptoCurrency;

    private String value;
    private String amount;

    public static final BigDecimal RAW_PER_NANO = new BigDecimal("1000000000000000000000000000000");

    @Override
    public String toString() {
        return "value: " + value + ", amount: " + amount;
    }

    @Deprecated
    public Address(String value) {
        this(value, CryptoCurrency.NOLLAR);
    }

    public Address(String value, CryptoCurrency cryptoCurrency) {
        this.value = value;
        this.cryptoCurrency = cryptoCurrency;
        parseAddress();
    }

    public boolean haCryptoCurrencyAddressFormat() {
        return value.contains(cryptoCurrency.getPrefix());
    }

    public String getShortString() {
        int frontStartIndex = 0;
        int frontEndIndex = haCryptoCurrencyAddressFormat() ? 9 : 10;
        int backStartIndex = value.length() - 5;
        return value.substring(frontStartIndex, frontEndIndex) +
                "..." +
                value.substring(backStartIndex, value.length());
    }

    public Spannable getColorizedShortSpannable() {
        Spannable s = new SpannableString(getShortString());
        int frontStartIndex = 0;
        int frontEndIndex = haCryptoCurrencyAddressFormat() ? 9 : 10;
        int backStartIndex = s.length() - 5;

        // colorize the string
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#4a90e2")), frontStartIndex, frontEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#e1990e")), backStartIndex, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public String getAddress() {
        return value;
    }

    public String getAddressWithoutPrefix() {
        return value.replace(cryptoCurrency.getPrefix(), "");
    }

    public String getAmount() {
        return amount;
    }

    public boolean isValidAddress() {
        if (value == null) {
            Log.w(TAG, "isValidAddress: value is null");
            return false;
        }

        String[] parts = value.split("_");
        if (parts.length != 2) {
            Log.w(TAG, "isValidAddress: parts are not ok");

            return false;
        }
        if (!parts[0].equals(cryptoCurrency.getPrefixWithNoFloor()) &&
                !parts[0].equals("nano")
                ) {
            Log.w(TAG, "isValidAddress: #3");
            return false;
        }
        final int addressLength = parts[1].length();

        if (addressLength != 60) {
            Log.w(TAG, "isValidAddress: #4 actual length == " + addressLength);
            return false;
        }
        checkCharacters:
        for (int i = 0; i < parts[1].length(); i++) {
            char letter = parts[1].toLowerCase().charAt(i);
            for (int j = 0; j < NOSUtil.addressCodeCharArray.length; j++) {
                if (NOSUtil.addressCodeCharArray[j] == letter) {
                    continue checkCharacters;
                }
            }
            Log.w(TAG, "isValidAddress: #5");
            return false;
        }
        byte[] shortBytes = NOSUtil.hexToBytes(NOSUtil.decodeAddressCharacters(parts[1]));
        byte[] bytes = new byte[37];
        // Restore leading null bytes
        System.arraycopy(shortBytes, 0, bytes, bytes.length - shortBytes.length, shortBytes.length);
        byte[] checksum = new byte[5];
        byte[] state = new byte[Sodium.crypto_generichash_statebytes()];
        byte[] key = new byte[Sodium.crypto_generichash_keybytes()];
        NaCl.sodium();
        Sodium.crypto_generichash_blake2b_init(state, key, 0, 5);
        Sodium.crypto_generichash_blake2b_update(state, bytes, 32);
        Sodium.crypto_generichash_blake2b_final(state, checksum, checksum.length);
        for (int i = 0; i < checksum.length; i++) {
            if (checksum[i] != bytes[bytes.length - 1 - i]) {
                Log.w(TAG, "isValidAddress: bad checksum");
                return false;
            }
        }
        return true;
    }

    private boolean isNanoAddress(int addressLength) {
        return addressLength == 59 && cryptoCurrency == CryptoCurrency.NANO;
    }

    private void parseAddress() {
        if (this.value != null) {
            String[] _split = value.split(":");
            if (_split.length > 1) {
                String _addressString = _split[1];
                Uri uri = Uri.parse(_addressString);
                if (uri.getPath() != null) {
                    this.value = uri.getPath();
                }
                if (uri.getQueryParameter("amount") != null && !uri.getQueryParameter("amount").equals("")) {
                    try {
                        this.amount = (new BigDecimal(uri.getQueryParameter("amount")).divide(RAW_PER_NANO)).toString();
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

    }
}

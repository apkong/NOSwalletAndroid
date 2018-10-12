package co.nos.noswallet.persistance;

import co.nos.noswallet.persistance.currency.CryptoCurrency;

public interface UserSession {

    CryptoCurrency getPreferredCurrencyPrefix();

    void setPreferredCurrencyPrefix(CryptoCurrency currency);
}

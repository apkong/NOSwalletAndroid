package co.nos.noswallet.ui.settings.addressBook;

import java.io.Serializable;
import java.util.HashMap;

import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class AddressBookEntry implements Serializable {

    public final String name;
    public final HashMap<CryptoCurrency, String> addressesMap;

    public AddressBookEntry(String name, HashMap<CryptoCurrency, String> addressesMap) {
        this.name = name;
        this.addressesMap = addressesMap;
    }
}

package co.nos.noswallet.ui.settings.addressBook.addressDetail;

import android.util.Log;

import java.util.Map;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.settings.addressBook.AddressBookEntry;
import co.nos.noswallet.ui.settings.addressBook.repository.AddressBook;
import co.nos.noswallet.ui.settings.addressBook.repository.AddressbookRepository;
import io.reactivex.disposables.Disposable;

public class AddressDetailPresenter {

    private AddressDetailView view;

    private AddressbookRepository addressbookRepository;

    @Inject
    public AddressDetailPresenter(AddressbookRepository addressbookRepository) {
        this.addressbookRepository = addressbookRepository;
    }

    public void attachView(AddressDetailView view) {
        this.view = view;
    }

    private boolean isValidAddress(String address, CryptoCurrency currency) {
        if (!address.isEmpty() && address.length() != 64) {
            view.showErrorMessage(R.string.invalid_account_number_length_placeholder, currency);
            return false;
        }
        return true;
    }

    public void delete(AddressBookEntry entry) {
        AddressBook addressBookEntries = addressbookRepository.getAddressBook().blockingFirst();//for now its not network based...

        AddressBook newAddressBook = new AddressBook();

        for (AddressBookEntry address : addressBookEntries) {
            if (!String.valueOf(address.name).equals(entry.name)) {
                newAddressBook.add(address);
            }
        }

        persist(addressBookEntries, () -> {
            view.onAddressDeleted();
        });
    }

    private void persist(AddressBook addressBook, Runnable runnable) {

        Disposable d = addressbookRepository.saveAddressBook(addressBook)
                .subscribe(runnable::run, throwable -> {
                    Log.e(getClass().getSimpleName(),
                            "addAddress() error " + throwable.getMessage());

                });
    }

    public void save(AddressBookEntry entry,
                     String nollar,
                     String nos,
                     String banano,
                     String nano) {
        view.clearErrors();

        if (!isValidAddress(nollar, CryptoCurrency.NOLLAR)) {
            return;
        }
        if (!isValidAddress(nos, CryptoCurrency.NOS)) {
            return;
        }

        if (!isValidAddress(banano, CryptoCurrency.BANANO)) {
            return;
        }
        if (!isValidAddress(nano, CryptoCurrency.NANO)) {
            return;
        }


        AddressBook addressBookEntries = addressbookRepository.getAddressBook().blockingFirst();//for now its not network based...

        for (AddressBookEntry address : addressBookEntries) {
            if (String.valueOf(address.name).equals(entry.name)) {
                address.addressesMap.put(CryptoCurrency.NOLLAR, nollar);
                address.addressesMap.put(CryptoCurrency.NOS, nos);
                address.addressesMap.put(CryptoCurrency.BANANO, banano);
                address.addressesMap.put(CryptoCurrency.NANO, nano);
            }
        }

        persist(addressBookEntries, () -> view.onAddressSaved());


    }

    public String resolveFrom(Map<CryptoCurrency, String> map, CryptoCurrency currency) {
        for (CryptoCurrency c : map.keySet()) {
            String address = map.get(c);
            if (address != null) {
                return address.replace(c.getPrefix(), currency.getPrefix());
            }
        }
        return "";
    }
}

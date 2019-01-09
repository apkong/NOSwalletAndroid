package co.nos.noswallet.ui.settings.addressBook.addAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.settings.addressBook.AddressBookEntry;
import co.nos.noswallet.ui.settings.addressBook.repository.AddressBook;
import co.nos.noswallet.ui.settings.addressBook.repository.AddressbookRepository;
import io.reactivex.disposables.Disposable;

public class AddAddressPresenter {

    private AddAddressView view;

    private AddressbookRepository addressbookRepository;

    @Inject
    public AddAddressPresenter(AddressbookRepository addressbookRepository) {
        this.addressbookRepository = addressbookRepository;
    }

    public void attachView(AddAddressView view) {
        this.view = view;
    }

    public void addAddress(CryptoCurrency cryptoCurrency,
                           String name,
                           String address) {
        view.clearErrors();
        if (address.length() != 64) {
            view.showErrorMessage(R.string.invalid_account_number_length);
            return;
        }

        HashMap<CryptoCurrency, String> map = new HashMap<>();
        map.put(cryptoCurrency, address);
        AddressBookEntry entry = new AddressBookEntry(name, map);


        AddressBook addressBookEntries = addressbookRepository.getAddressBook().blockingFirst();//for now its not network based...
        addressBookEntries.add(entry);

        Disposable d = addressbookRepository.saveAddressBook(addressBookEntries)
                .subscribe(() -> {
                    view.onAddressSaved(entry);
                }, throwable -> {

                });


    }
}

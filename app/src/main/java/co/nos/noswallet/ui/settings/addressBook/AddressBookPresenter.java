package co.nos.noswallet.ui.settings.addressBook;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.ui.settings.addressBook.repository.AddressbookRepository;
import io.reactivex.Observable;

public class AddressBookPresenter {

    private final AddressbookRepository addressbookRepository;
    private AddressBookView view;

    private List<AddressBookEntry> entries = new ArrayList<>();

    public boolean canFilterAnything() {
        return !entries.isEmpty();
    }

    @Inject
    public AddressBookPresenter(AddressbookRepository repository) {
        addressbookRepository = repository;
    }

    public void attachView(AddressBookView view) {
        this.view = view;
    }


    public void loadEntries() {
        this.entries = addressbookRepository.getAddressBook().blockingFirst();
        view.clearSearchAndReceive(this.entries);
    }

    public void filterAddresses(String query) {
        if (query.length() == 0) {
            showEntriesOrEmpty(entries);
        } else {
            List<AddressBookEntry> filteredEntries = filteredEntries(query);
            showEntriesOrEmpty(filteredEntries);
        }
    }

    private void showEntriesOrEmpty(List<AddressBookEntry> entries) {
        if (entries.isEmpty()) {
            view.showErrorMessage(R.string.no_matching_entries_found);
        } else {
            view.onReceivedAddresses(entries);
        }
    }

    private List<AddressBookEntry> filteredEntries(String query) {
        return Observable.fromIterable(entries)
                .filter(addressBookEntry -> matchesFilter(addressBookEntry, query))
                .toList()
                .blockingGet();
    }

    private boolean matchesFilter(AddressBookEntry addressBookEntry, String query) {
        if (String.valueOf(addressBookEntry.name).toLowerCase().startsWith(query.toLowerCase())) {
            return true;
        }
        for (String value : addressBookEntry.addressesMap.values()) {
            if (value.startsWith(query)) {
                return true;
            }
        }
        return false;
    }

    public void requestAddAddressScreen() {
        view.navigateToAddAddressScreen();
    }

    public void onAddressEntryClick(AddressBookEntry addressBookEntry) {
        view.navigateToAddressEntryDetail(addressBookEntry);
    }
}

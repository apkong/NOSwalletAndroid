package co.nos.noswallet.ui.settings.addressBook;

import java.util.List;

public interface AddressBookView {

    void showErrorMessage(int resId);

    void onReceivedAddresses(List<AddressBookEntry> entries);

    void clearSearchAndReceive(List<AddressBookEntry> entries);

    void navigateToAddAddressScreen();

    void navigateToAddressEntryDetail(AddressBookEntry addressBookEntry);

    void navigateBackWithResult(AddressBookEntry addressBookEntry);
}

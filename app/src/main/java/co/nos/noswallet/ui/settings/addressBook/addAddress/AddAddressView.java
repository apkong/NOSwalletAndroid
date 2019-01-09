package co.nos.noswallet.ui.settings.addressBook.addAddress;

import co.nos.noswallet.ui.settings.addressBook.AddressBookEntry;

public interface AddAddressView {

    void showErrorMessage(int resId);

    void onAddressSaved(AddressBookEntry entry);

    void clearErrors();
}

package co.nos.noswallet.ui.settings.addressBook.addressDetail;

import co.nos.noswallet.persistance.currency.CryptoCurrency;

public interface AddressDetailView {

    void showErrorMessage(int resId);

    void showErrorMessage(int resId, CryptoCurrency currency);

    void onAddressSaved();

    void onAddressDeleted();

    void clearErrors();
}

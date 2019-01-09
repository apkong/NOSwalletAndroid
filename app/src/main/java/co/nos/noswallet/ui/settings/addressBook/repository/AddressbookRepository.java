package co.nos.noswallet.ui.settings.addressBook.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;

public interface AddressbookRepository {

    Observable<AddressBook> getAddressBook();

    Completable saveAddressBook(AddressBook addressBook);
}

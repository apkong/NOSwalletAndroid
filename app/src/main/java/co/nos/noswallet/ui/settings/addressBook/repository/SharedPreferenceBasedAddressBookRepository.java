package co.nos.noswallet.ui.settings.addressBook.repository;

import com.google.gson.JsonSyntaxException;

import javax.inject.Inject;

import co.nos.noswallet.util.S;
import co.nos.noswallet.util.SharedPreferencesUtil;
import io.reactivex.Completable;
import io.reactivex.Observable;

public class SharedPreferenceBasedAddressBookRepository implements AddressbookRepository {

    private SharedPreferencesUtil sharedPreferencesUtil;

    public static final String KEY = "SharedPreferenceBasedAddressBookRepository";

    @Inject
    public SharedPreferenceBasedAddressBookRepository(SharedPreferencesUtil sharedPreferencesUtil) {
        this.sharedPreferencesUtil = sharedPreferencesUtil;
    }

    @Override
    public Observable<AddressBook> getAddressBook() {

        String json = sharedPreferencesUtil.get(KEY, "");
        if (!json.isEmpty()) {
            try {

                AddressBook addressBookEntries = S.GSON.fromJson(json, AddressBook.class);
                return Observable.fromCallable(() -> addressBookEntries);
            } catch (JsonSyntaxException x) {

            }
        }

        return Observable.fromCallable(AddressBook::new);
    }

    @Override
    public Completable saveAddressBook(AddressBook addressBook) {
        boolean result = sharedPreferencesUtil.getEditor()
                .putString(KEY, S.GSON.toJson(addressBook))
                .commit();

        return result ? Completable.complete() : Completable.error(new Exception("failed to save to shared preferences!!!"));
    }
}

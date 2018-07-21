package co.nos.noswallet.kyc.homeAddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class CountriesRepository {

    @Inject
    CountriesRepository() {
    }

    public Observable<List<Country>> getCountriesAsync() {

        return Observable.fromCallable(this::getCountries);
    }

    public List<Country> getCountries() {
        List<Country> countries = new ArrayList<>(Arrays.asList(
                new Country("uk", "United Kingdom"),
                new Country("de", "Germany"),
                new Country("es", "Spain"),
                new Country("it", "Italy")
        ));
        return countries;
    }
}

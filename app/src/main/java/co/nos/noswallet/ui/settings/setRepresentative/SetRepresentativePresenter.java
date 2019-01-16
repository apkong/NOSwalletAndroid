package co.nos.noswallet.ui.settings.setRepresentative;

import android.support.annotation.VisibleForTesting;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.db.RepresentativesProvider;
import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class SetRepresentativePresenter {

    private SetRepresentativeView view;

    private final RepresentativesProvider representativesProvider;

    @VisibleForTesting
    protected String previousRepresentative;

    @Inject
    public SetRepresentativePresenter(RepresentativesProvider representativesProvider) {
        this.representativesProvider = representativesProvider;
    }

    public void attachView(SetRepresentativeView view) {
        this.view = view;
    }

    public void requestCachedRepresentative(CryptoCurrency currency) {
        previousRepresentative = representativesProvider.provideRepresentative(currency);

        view.onRepresentativeReceived(previousRepresentative);
    }

    public void saveRepresentativeClicked(CryptoCurrency currency, String representative) {
        view.clearRepresentativeError();

        boolean newRepresentativeInvalid = representativeEmpty(representative)
                || representativeWithWrongCurrency(representative, currency)
                || representativeHasWrongLength(representative);

        if (newRepresentativeInvalid) {
            view.showRepresentativeError(R.string.invalid_representative);
        } else {
            representativesProvider.setOwnRepresentative(currency, representative);
            view.showRepresentativeSavedAndExit(R.string.representative_saved);
        }
    }

    private boolean representativeHasWrongLength(String representative) {
        if (representative == null || previousRepresentative == null) {
            return false;
        }
        return previousRepresentative.length() != representative.length();
    }

    private boolean representativeWithWrongCurrency(String representative, CryptoCurrency currency) {
        return !representative.startsWith(currency.getPrefix());
    }

    private boolean representativeEmpty(String representative) {
        return representative == null || representative.isEmpty();
    }
}

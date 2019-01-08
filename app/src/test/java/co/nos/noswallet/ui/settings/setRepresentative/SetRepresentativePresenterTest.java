package co.nos.noswallet.ui.settings.setRepresentative;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import co.nos.noswallet.R;
import co.nos.noswallet.db.RepresentativesProvider;
import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class SetRepresentativePresenterTest {

    SetRepresentativePresenter presenter;

    @Mock
    RepresentativesProvider provider;

    @Mock
    SetRepresentativeView view;

    CryptoCurrency cryptoCurrency = CryptoCurrency.NOLLAR;
    String representative = "usd_39nqgscm7yz7q1demb3zf96ru8bboqcwet14cefhmgbrxbhhozhjwxci1k9m";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = new SetRepresentativePresenter(provider);
        presenter.attachView(view);
    }

    @Test
    public void shouldRequestCachedRepresentative() {
        String value = "value";
        Mockito.when(provider.provideRepresentative(cryptoCurrency)).thenReturn(value);

        presenter.requestCachedRepresentative(cryptoCurrency);

        Mockito.verify(view).onRepresentativeReceived(value);

        Assert.assertEquals(value, presenter.previousRepresentative);
    }

    @Test
    public void saveRepresentativeClicked() {
        presenter.previousRepresentative = representative;

        presenter.saveRepresentativeClicked(cryptoCurrency, representative);

        Mockito.verify(view).clearRepresentativeError();
        Mockito.verify(provider).setOwnRepresentative(cryptoCurrency, representative);
        Mockito.verify(view).showRepresentativeSavedAndExit(R.string.representative_saved);
    }
}

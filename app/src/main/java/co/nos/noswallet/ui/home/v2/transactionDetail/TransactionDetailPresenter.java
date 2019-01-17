package co.nos.noswallet.ui.home.v2.transactionDetail;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.refundable.RefundableBundle;

public class TransactionDetailPresenter {

    private TransactionDetailView view;

    private Map<CryptoCurrency, String> blockHashMap = new HashMap<CryptoCurrency, String>() {{
        put(CryptoCurrency.NOS, "https://scan.nos.cash/explorer/block/");
        put(CryptoCurrency.NOLLAR, "https://scan.nollar.org/explorer/block/");
        put(CryptoCurrency.BANANO, "https://creeper.banano.cc/explorer/block/");
        put(CryptoCurrency.NANO, "https://nanocrawler.cc/explorer/block/");
    }};

    private final CryptoCurrencyFormatter cryptoCurrencyFormatter;

    @Inject
    public TransactionDetailPresenter(CryptoCurrencyFormatter formatter) {
        this.cryptoCurrencyFormatter = formatter;
    }

    public void attachView(TransactionDetailView view) {
        this.view = view;
    }

    public void attemptRefund(CryptoCurrency cryptoCurrency, AccountHistory entry) {

        view.navigateToRefundScreen(new RefundableBundle(cryptoCurrency, entry.amount, entry.account));
    }

    public void openBlockHashLink(CryptoCurrency cryptoCurrency, String hash) {
        String prefixUrl = blockHashMap.get(cryptoCurrency);

        view.onBlockHashLinkReceived(prefixUrl + hash);
    }

    public void fillWith(AccountHistory entry, CryptoCurrency currency) {
        cryptoCurrencyFormatter.useCurrency(currency);
        String formattedAmount = cryptoCurrencyFormatter.rawtoUi(entry.amount);

        if (entry.isSend()) {

            String formattedMessage = view.getString(R.string.sent_funds, formattedAmount, currency.name());

            view.onReceiveTransferParams(
                    R.drawable.ic_send, formattedMessage,
                    false,
                    entry.hash, entry.account
            );
        } else {
            String formattedMessage = view.getString(R.string.received_funds, formattedAmount, currency.name());

            view.onReceiveTransferParams(
                    R.drawable.ic_receive, formattedMessage,
                    true,
                    entry.hash, entry.account

            );
        }
    }
}

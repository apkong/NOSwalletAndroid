package co.nos.noswallet.network.websockets.currencyFormatter;

import javax.inject.Inject;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.push.HandlePushMessagesService;

public class CryptoCurrencyFormatter {

    private CryptoCurrency cryptoCurrency;


    @Inject
    public CryptoCurrencyFormatter() {
    }

    public CryptoCurrencyFormatter(CryptoCurrency cryptoCurrency) {
        this.cryptoCurrency = cryptoCurrency;
    }

    public CryptoCurrencyFormatter useCurrency(CryptoCurrency cryptoCurrency) {
        this.cryptoCurrency = cryptoCurrency;
        return this;
    }

    /**
     * formats uiValue to raw. (20_000 raw stands for 200.00)
     *
     * @param uiValue
     * @return
     */
    public String uiToRaw(String uiValue) {
        return cryptoCurrency.uiToRaw(uiValue);
    }

    public String rawtoUi(String raw) {
        return cutOutTrailingZeros(cryptoCurrency.rawToUi(raw));
    }

    public String cutOutTrailingZeros(String expression) {
        return HandlePushMessagesService.formatWell(expression);
    }
}

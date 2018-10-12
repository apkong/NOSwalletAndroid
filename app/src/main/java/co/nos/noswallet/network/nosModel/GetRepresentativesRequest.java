package co.nos.noswallet.network.nosModel;

import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class GetRepresentativesRequest extends BaseWebsocketRequest{

    @Override
    public String getActionName() {
        return "representatives";
    }

    @Override
    public String getCurrencyCode() {
        return CryptoCurrency.NOLLAR.getCurrencyCode();
    }

    public GetRepresentativesRequest() {
    }
}

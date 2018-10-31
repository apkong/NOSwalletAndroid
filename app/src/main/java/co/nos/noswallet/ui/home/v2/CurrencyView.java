package co.nos.noswallet.ui.home.v2;

import java.util.ArrayList;

import co.nos.noswallet.network.nosModel.AccountHistory;

public interface CurrencyView {
    boolean isNotAttached();

    void showHistory(ArrayList<AccountHistory> history);

    void onBalanceFormattedReceived(String formatted);


}

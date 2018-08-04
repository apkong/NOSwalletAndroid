package co.nos.noswallet.ui.home;

import android.support.annotation.StringRes;

import java.util.ArrayList;

import co.nos.noswallet.base.BaseView;
import co.nos.noswallet.network.nosModel.AccountHistory;

public interface HomeView extends BaseView {

    void showHistory(ArrayList<AccountHistory> history);

    void showHistoryEmpty();

    void onBalanceFormattedReceived(String formatted);

    String getString(@StringRes int resId);

    void showError(String string);
}

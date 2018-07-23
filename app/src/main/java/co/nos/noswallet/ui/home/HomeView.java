package co.nos.noswallet.ui.home;

import java.util.ArrayList;

import co.nos.noswallet.base.BaseView;
import co.nos.noswallet.network.nosModel.AccountHistory;

public interface HomeView extends BaseView {
    void showHistory(ArrayList<AccountHistory> history);

    void showHistoryEmpty();

}

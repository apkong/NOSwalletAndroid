package co.nos.noswallet.ui.home;

import java.util.ArrayList;

import javax.inject.Inject;

import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.network.interactor.GetHistoryUseCase;
import co.nos.noswallet.network.nosModel.AccountHistory;
import io.reactivex.disposables.SerialDisposable;

public class HomePresenter extends BasePresenter<HomeView> {

    SerialDisposable serialDisposable = new SerialDisposable();
    private final GetHistoryUseCase getHistoryUseCase;

    @Inject
    public HomePresenter(GetHistoryUseCase getHistoryUseCase) {
        this.getHistoryUseCase = getHistoryUseCase;
    }

    public void requestUpdate() {

        view.showLoading();

        serialDisposable.set(getHistoryUseCase.execute("xrb_3i1aq1cchnmbn9x5rsbap8b15akfh7wj7pwskuzi7ahz8oq6cobd99d4r3b7")
                .subscribe(neuroHistoryResponse -> {
                    view.hideLoading();
                    ArrayList<AccountHistory> hist = neuroHistoryResponse.history;
                    if (hist == null || hist.isEmpty()) {
                        //todo:
                        view.showHistoryEmpty();
                    }
                    view.showHistory(neuroHistoryResponse.history);
                }, throwable -> {
                    view.hideLoading();
                    view.showHistoryEmpty();
                }));
        addDisposable(serialDisposable);
    }

    public void requestPending() {
        //todo:
    }
}

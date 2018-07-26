package co.nos.noswallet.ui.home;

import java.util.ArrayList;

import javax.inject.Inject;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.network.interactor.GetHistoryUseCase;
import co.nos.noswallet.network.interactor.GetPendingBlocksUseCase;
import co.nos.noswallet.network.nosModel.AccountHistory;
import io.reactivex.disposables.SerialDisposable;

public class HomePresenter extends BasePresenter<HomeView> {

    SerialDisposable historyDisposable = new SerialDisposable();
    private final GetHistoryUseCase getHistoryUseCase;

    @Inject
    public HomePresenter(GetHistoryUseCase getHistoryUseCase) {
        this.getHistoryUseCase = getHistoryUseCase;
    }

    public void requestUpdateHistory() {

        view.showLoading();

        historyDisposable.set(getHistoryUseCase.execute()
                .subscribe(neuroHistoryResponse -> {
                    view.hideLoading();
                    ArrayList<AccountHistory> hist = neuroHistoryResponse.history;
                    if (NOSUtil.isEmpty(hist)) {
                        //todo:
                        view.showHistoryEmpty();
                    }
                    view.showHistory(neuroHistoryResponse.history);
                }, throwable -> {
                    view.hideLoading();
                    view.showHistoryEmpty();
                }));
        addDisposable(historyDisposable);
    }


}

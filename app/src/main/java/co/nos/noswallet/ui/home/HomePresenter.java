package co.nos.noswallet.ui.home;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.network.interactor.GetHistoryUseCase;
import co.nos.noswallet.network.interactor.GetPendingBlocksUseCase;
import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.network.nosModel.GetPendingBlocksResponse;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.SerialDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class HomePresenter extends BasePresenter<HomeView> {

    SerialDisposable historyDisposable = new SerialDisposable();
    SerialDisposable pendingTransactionsDisposable = new SerialDisposable();
    private final GetHistoryUseCase getHistoryUseCase;
    private final GetPendingBlocksUseCase getPendingBlocksUseCase;

    @Inject
    public HomePresenter(GetHistoryUseCase getHistoryUseCase,
                         GetPendingBlocksUseCase getPendingBlocksUseCase) {
        this.getHistoryUseCase = getHistoryUseCase;
        this.getPendingBlocksUseCase = getPendingBlocksUseCase;
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


    public void requestPending() {
        getPendingBlocksUseCase.startObservePendingTransactions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPendingBlocksUseCase.stopPendingTransactions();
    }
}

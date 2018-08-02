package co.nos.noswallet.ui.home;

import java.util.ArrayList;

import javax.inject.Inject;

import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.model.NOSWallet;
import co.nos.noswallet.network.interactor.CheckAccountBalanceUseCase;
import co.nos.noswallet.network.interactor.GetHistoryUseCase;
import co.nos.noswallet.network.interactor.GetPendingBlocksUseCase;
import co.nos.noswallet.network.nosModel.AccountHistory;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.SerialDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HomePresenter extends BasePresenter<HomeView> {

    SerialDisposable historyDisposable = new SerialDisposable();
    private final GetHistoryUseCase getHistoryUseCase;
    private final CheckAccountBalanceUseCase checkAccountBalanceUseCase;
    private final GetPendingBlocksUseCase getPendingBlocksUseCase;


    @Inject
    public HomePresenter(GetHistoryUseCase getHistoryUseCase,
                         GetPendingBlocksUseCase getPendingBlocksUseCase,
                         CheckAccountBalanceUseCase checkAccountBalanceUseCase) {
        this.getHistoryUseCase = getHistoryUseCase;
        this.checkAccountBalanceUseCase = checkAccountBalanceUseCase;
        this.getPendingBlocksUseCase = getPendingBlocksUseCase;
    }

    public void requestUpdateHistory() {

        view.showLoading();

        if (getHistoryUseCase.cachedResponse != null) {
            view.showHistory(getHistoryUseCase.cachedResponse.history);
        }

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

    public void checkAccountBalance() {
        addDisposable(checkAccountBalanceUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        String amount = NOSWallet.rawToNeuros(s);
                        NOSApplication.getNosWallet().setNeuros(amount);
                        view.onBalanceFormattedReceived(amount + " NOS");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.err.println("error: " + throwable);
                    }
                }));
    }

    public void onStart() {
        getPendingBlocksUseCase.startObservePendingTransactions();
    }
}

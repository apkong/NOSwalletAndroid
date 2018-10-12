package co.nos.noswallet.ui.home;

import com.google.gson.JsonElement;

import java.util.ArrayList;

import javax.inject.Inject;

import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.R;
import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.model.NOSWallet;
import co.nos.noswallet.network.interactor.CheckAccountBalanceUseCase;
import co.nos.noswallet.network.interactor.GetHistoryUseCase;
import co.nos.noswallet.network.interactor.GetPendingBlocksUseCase;
import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HomePresenter extends BasePresenter<HomeView> {

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
        if (getHistoryUseCase.cachedResponse != null) {
            view.showHistory(getHistoryUseCase.cachedResponse.history);
        }

        view.showLoading();
        addDisposable(getHistoryUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(neuroHistoryResponse -> {
                    view.hideLoading();
                    ArrayList<AccountHistory> history = neuroHistoryResponse.history;
                    if (NOSUtil.isEmpty(history)) {
                        view.showHistoryEmpty();
                    } else {
                        view.showHistory(history);
                    }
                }, throwable -> {
                    view.hideLoading();
                    view.showError(view.getString(R.string.failed_to_receive_history));
                }));

    }

    public void requestAccountBalanceCheck() {
        addDisposable(checkAccountBalanceUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(balance -> {

                    String amount = NOSWallet.rawToNeuros(balance);
                    NOSApplication.getNosWallet().setNeuros(amount);
                    view.onBalanceFormattedReceived(amount + " NOS");

                }, throwable -> System.err.println("error: " + throwable)));
    }

    public void onStart() {
        this.requestUpdateHistory();
        this.requestAccountBalanceCheck();
        getPendingBlocksUseCase.startObservePendingTransactions();
    }


    private Disposable getHistoryDisposable = null;

    public void observeUiCallbacks(WebsocketMachine websocketMachine) {
        if (getHistoryDisposable != null) {
            getHistoryDisposable.dispose();
        }
        getHistoryDisposable = (websocketMachine.observeUiTriggers()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WebsocketMachine.SocketResponse>() {
                    @Override
                    public void accept(WebsocketMachine.SocketResponse response) throws Exception {
                        if (response.isHistoryResponse()) {
                            JsonElement element = response.response;

                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
    }
}

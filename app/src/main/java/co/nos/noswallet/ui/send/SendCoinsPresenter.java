package co.nos.noswallet.ui.send;

import android.util.Log;

import java.math.BigDecimal;

import javax.inject.Inject;

import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.R;
import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.model.Address;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.model.NeuroWallet;
import co.nos.noswallet.network.nosModel.SocketResponse;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.SerialDisposable;
import io.reactivex.functions.Consumer;
import io.realm.Realm;

public class SendCoinsPresenter extends BasePresenter<SendCoinsView> {

    public static final String TAG = SendCoinsPresenter.class.getSimpleName();

    private String targetAddress;

    private String currentInput = "";

    private final NeuroWallet nosWallet = NOSApplication.getNosWallet();

    private final Realm realm;
    private String recentTypedCoins = "";


    @Inject
    public SendCoinsPresenter(Realm realm) {
        this.realm = realm;
    }

    public void attemptSendCoins(String coinsAmount) {
        Log.w(TAG, "attemptSendCoins: " + coinsAmount);
        if (!targetAddressValid()) {
            String message = view.getString(R.string.please_specify_destination_address);
            view.showError(message);
            return;
        }

        System.out.println("execute send()");

        if (canTransferNeuros(coinsAmount)) {
            view.showLoading();

            String sendAmount = (coinsAmount);

            if (websocketMachineRef != null) {
                websocketMachineRef.transferCoins(sendAmount, targetAddress);
                view.showLoading();
            }

//            addDisposable(sendCoinsUseCase.transferCoins(sendAmount, targetAddress)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Consumer<ProcessResponse>() {
//                        @Override
//                        public void accept(ProcessResponse o) throws Exception {
//                            view.hideLoading();
//                            BigDecimal dec = new BigDecimal(sendAmount).divide(new BigDecimal("10").pow(30), RoundingMode.DOWN);
//                            view.showAmountSent(dec.toEngineeringString(), targetAddress);
//                        }
//                    }, new Consumer<Throwable>() {
//                        @Override
//                        public void accept(Throwable throwable) throws Exception {
//                            view.hideLoading();
//                            System.err.println("error sending coins " + throwable);
//                            throwable.printStackTrace();
//                            view.showError(R.string.send_error_alert_title, R.string.send_error_alert_message);
//                        }
//                    }));
        } else {
            view.showError(R.string.send_error_alert_title, R.string.cannot_transfer);
        }
    }

    public void setTargetAddress(String address) {
        targetAddress = address;
    }

    public void onDeleteTyped() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            view.onCurrentInputReceived(currentInput);
        } else {
            view.onCurrentInputReceived(currentInput = "0");
        }
    }

    public void onDotTyped() {
        if (!currentInput.isEmpty()) {
            if (!alreadyHaveDot(currentInput)) {
                currentInput = currentInput + ".";
                view.onCurrentInputReceived(currentInput);
            }
        }
    }

    private boolean alreadyHaveDot(String currentInput) {
        char[] array = currentInput.toCharArray();
        for (char ch : array) {
            if (ch == '.') return true;
        }
        return false;
    }

    public void onNumberTyped(String input) {
        if (currentInput.equalsIgnoreCase("0")) {
            currentInput = input;
        } else {
            currentInput = currentInput + input;
        }
        view.onCurrentInputReceived(currentInput);
    }

    public void updateAmountFromCode(CharSequence totalValue) {
        currentInput = totalValue.toString();
        view.onCurrentInputReceived(currentInput);
    }

    public void updateAmount(CharSequence value) {
        if (value.equals(view.getString(R.string.send_keyboard_delete))) {
            //delete performed
            onDeleteTyped();
        } else if (value.equals(view.getString(R.string.send_keyboard_decimal))) {
            // decimal point
            onDotTyped();
        } else {
            // digits
            onNumberTyped(value.toString());
        }
    }

    public Credentials provideCredentials() {
        return realm.where(Credentials.class).findFirst();
    }

    public boolean canTransferNeuros(String currentTypedCoins) {
        this.recentTypedCoins = currentTypedCoins;
        if (currentTypedCoins == null || currentTypedCoins.isEmpty() || new BigDecimal(currentTypedCoins).equals(BigDecimal.ZERO))
            return false;
        if (websocketMachineRef != null) {
            return nosWallet.canTransferNeuros(currentTypedCoins, websocketMachineRef.recentAccountBalance);
        }
        return false;
    }

    private boolean targetAddressValid() {
        Address destination = new Address(targetAddress);
        return destination.isValidAddress();
    }

    public String getTotalNeurosAmount() {
        String balance = null;
        if (websocketMachineRef != null) {
            balance = websocketMachineRef.recentAccountBalance;
            NOSApplication.getNosWallet().setNeuros(balance);
        }
        Log.d(TAG, "getTotalNeurosAmount: " + balance);
        return balance;
    }

    private WebsocketMachine websocketMachineRef;

    public void observeWebsocketMachine(WebsocketMachine machine) {
        websocketMachineRef = machine;
        serialDisposable.set(machine.observeUiTriggers()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<SocketResponse>() {
                    @Override
                    public void accept(SocketResponse response) throws Exception {
                        if (response.isProcessedBlock()) {
                            view.hideLoading();
                            if (response.error != null) {
                                view.showError(response.error);
                            } else {
                                view.showAmountSent(recentTypedCoins, targetAddress);
                            }
                        }
                    }
                }, this::onErrorSendCoins)
        );
    }

    private void onErrorSendCoins(Throwable throwable) {
        Log.e(TAG, "onErrorSendCoins: ", throwable);
        throwable.printStackTrace();
    }

    public void cancelWebsocketObservation() {
        Disposable d = serialDisposable.get();
        if (d != null && !d.isDisposed()) {
            d.dispose();
        }
    }

    private SerialDisposable serialDisposable = new SerialDisposable();
}

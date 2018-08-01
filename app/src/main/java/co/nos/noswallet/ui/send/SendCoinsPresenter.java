package co.nos.noswallet.ui.send;

import javax.inject.Inject;

import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.R;
import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.model.Address;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.model.NOSWallet;
import co.nos.noswallet.network.interactor.SendCoinsUseCase;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class SendCoinsPresenter extends BasePresenter<SendCoinsView> {

    private String targetAddress;

    private String currentInput = "";

    private final NOSWallet nosWallet = NOSApplication.getNosWallet();

    private final Realm realm;
    private final SendCoinsUseCase sendCoinsUseCase;


    @Inject
    public SendCoinsPresenter(Realm realm,
                              SendCoinsUseCase sendCoinsUseCase) {
        this.realm = realm;
        this.sendCoinsUseCase = sendCoinsUseCase;
    }


    public void attemptSendCoins(String coinsAmount) {

        if (!targetAddressValid()) {
            String message = view.getString(R.string.please_specify_destination_address);
            view.showError(message);
            return;
        }

        System.out.println("execute send()");

        if (canTransferNeuros(coinsAmount)) {
            view.showLoading();

            String sendAmount = nosWallet.getRawToTransfer(coinsAmount);

            addDisposable(sendCoinsUseCase.transferCoins(sendAmount, targetAddress)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object o) throws Exception {
                            view.hideLoading();
                            view.showAmountSent(sendAmount, targetAddress);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            view.showError(R.string.send_error_alert_title, R.string.send_error_alert_message);
                        }
                    }));


            //analyticsService.track(AnalyticsEvents.SEND_BEGAN);
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

    public void updateAmountFromCode(CharSequence totalValie) {
        currentInput = totalValie.toString();
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
        if (currentTypedCoins.isEmpty() || currentTypedCoins.equals("0")) return false;
        return nosWallet.canTransferNeuros(currentTypedCoins);
    }

    private boolean targetAddressValid() {
        Address destination = new Address(targetAddress);
        return destination.isValidAddress();
    }

    public String getTotalNeurosAmount() {
        return nosWallet.getTotalNeurosAmount();
    }
}

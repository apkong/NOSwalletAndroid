package co.nos.noswallet.kyc;

import android.util.Log;

import javax.inject.Inject;

import co.nos.noswallet.MainActivity;
import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.kyc.done.DoneFragment;
import co.nos.noswallet.kyc.emailAddress.EmailAddressFragment;
import co.nos.noswallet.kyc.homeAddress.HomeAddressFragment;
import co.nos.noswallet.kyc.identity.IdentityFragment;
import co.nos.noswallet.kyc.phoneNumber.PhoneNumberFragment;
import co.nos.noswallet.kyc.pinKeyboard.PinKeyboardFragment;
import co.nos.noswallet.kyc.smsCode.SmsCodeFragment;
import co.nos.noswallet.util.NosLogger;

public class KnowYourCustomerPresenter extends BasePresenter<KnowYourCustomerView> {

    public static final String TAG = KnowYourCustomerActivity.class.getSimpleName();

    private int currentPosition = 0;

    public static final int MAX_PAGE = 7;
    public static final int MULT = 1000;

    @Inject
    KnowYourCustomerPresenter() {

    }

    public void openFragmentWithCurrentPosition(int currentPosition) {
        NosLogger.d(TAG, "openFragmentWithCurrentPosition() called with: currentPosition = [" + currentPosition + "]");
        this.currentPosition = currentPosition;
        switch (currentPosition) {
            case 0: {
                view.navigateTo(new PhoneNumberFragment());
                break;
            }
            case 1: {
                view.navigateTo(new PinKeyboardFragment());
                break;
            }
            case 2: {
                view.navigateTo(new SmsCodeFragment());
                break;
            }
            case 3: {
                view.navigateTo(new IdentityFragment());
                break;
            }
            case 4: {
                view.navigateTo(new HomeAddressFragment());
                break;
            }
            case 5: {
                view.navigateTo(new EmailAddressFragment());
                break;
            }
            case 6: {
                view.navigateTo(new DoneFragment());
                break;
            }
            case 7: {
                view.startActivity(MainActivity.class);
                return;
            }
            default:
                System.out.println("current position: " + currentPosition);
                view.exitScreen();
                return;
        }

        view.updateSeekBarProgress(MULT * (currentPosition + 1));
    }


    public void setupSeekbar() {
        view.onSeekbarConfigurationChanged(MAX_PAGE * MULT);
    }


    public void onBackClicked() {
        openFragmentWithCurrentPosition(--currentPosition);
    }

    public void navigateNext(int position) {
        openFragmentWithCurrentPosition(position);
    }
}

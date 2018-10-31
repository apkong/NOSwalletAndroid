package co.nos.noswallet.di.activity;

import com.google.gson.Gson;

import co.nos.noswallet.MainActivity;
import co.nos.noswallet.di.application.ApplicationComponent;
import co.nos.noswallet.kyc.KnowYourCustomerActivity;
import co.nos.noswallet.kyc.KycUserDataRepository;
import co.nos.noswallet.kyc.done.DoneFragment;
import co.nos.noswallet.kyc.emailAddress.EmailAddressFragment;
import co.nos.noswallet.kyc.homeAddress.HomeAddressFragment;
import co.nos.noswallet.kyc.identity.IdentityFragment;
import co.nos.noswallet.kyc.phoneNumber.PhoneNumberFragment;
import co.nos.noswallet.kyc.pinKeyboard.PinKeyboardFragment;
import co.nos.noswallet.kyc.smsCode.SmsCodeFragment;
import co.nos.noswallet.model.NanoWallet;
import co.nos.noswallet.ui.home.HomeFragment;
import co.nos.noswallet.ui.home.v2.HistoryFragment;
import co.nos.noswallet.ui.intro.IntroLegalFragment;
import co.nos.noswallet.ui.intro.IntroNewWalletFragment;
import co.nos.noswallet.ui.intro.IntroSeedFragment;
import co.nos.noswallet.ui.intro.IntroWelcomeFragment;
import co.nos.noswallet.ui.pin.CreatePinDialogFragment;
import co.nos.noswallet.ui.pin.PinDialogFragment;
import co.nos.noswallet.ui.receive.ReceiveDialogFragment;
import co.nos.noswallet.ui.send.SendCoinsFragment;
import co.nos.noswallet.ui.send.SendFragment;
import co.nos.noswallet.ui.settings.SettingsDialogFragment;
import dagger.Component;

@Component(modules = {ActivityModule.class},
        dependencies = {ApplicationComponent.class})
@ActivityScope
public interface ActivityComponent {

    @ActivityScope
    KycUserDataRepository provideKycUserDataRepository();

    // wallet
    NanoWallet provideNanoWallet();

    @ActivityScope
    Gson provideGson();


    void inject(CreatePinDialogFragment createPinDialogFragment);

    void inject(HomeFragment homeFragment);

    void inject(IntroLegalFragment introLegalFragment);

    void inject(IntroNewWalletFragment introNewWalletFragment);

    void inject(IntroWelcomeFragment introWelcomeFragment);

    void inject(IntroSeedFragment introSeedFragment);

    void inject(MainActivity mainActivity);

    void inject(NanoWallet nanoWallet);

    void inject(PinDialogFragment pinDialogFragment);

    void inject(ReceiveDialogFragment receiveDialogFragment);

    void inject(SendFragment sendFragment);

    void inject(SettingsDialogFragment settingsDialogFragment);

    void inject(PhoneNumberFragment phoneNumberFragment);

    void inject(PinKeyboardFragment pinKeyboardFragment);

    void inject(SmsCodeFragment smsCodeFragment);

    void inject(IdentityFragment identityFragment);

    void inject(KnowYourCustomerActivity knowYourCustomerActivity);

    void inject(HomeAddressFragment homeAddressFragment);

    void inject(EmailAddressFragment emailAddressFragment);

    void inject(DoneFragment doneFragment);

    void inject(SendCoinsFragment sendCoinsFragment);

    void inject(HistoryFragment historyFragment);
}

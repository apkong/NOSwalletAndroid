package co.nos.noswallet.kyc;

import android.app.Activity;
import android.support.v4.app.Fragment;

import co.nos.noswallet.base.BaseView;

public interface KnowYourCustomerView extends BaseView {
    void navigateTo(Fragment fragment);
    void startActivity(Class<? extends Activity> klazz);
    void exitScreen();

    void onSeekbarConfigurationChanged(int max);

    void updateSeekBarProgress(int progress);
}

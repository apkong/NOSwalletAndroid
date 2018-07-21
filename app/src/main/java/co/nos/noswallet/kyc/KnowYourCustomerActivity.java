package co.nos.noswallet.kyc;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import javax.inject.Inject;

import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.R;
import co.nos.noswallet.base.BaseActivity;
import co.nos.noswallet.databinding.ActivityKnowYourCustomerBinding;

public class KnowYourCustomerActivity extends BaseActivity implements KnowYourCustomerView {
    public static final String TAG = "KYCActivity";

    int previousProgressValue = 0;

    @Inject
    KnowYourCustomerPresenter presenter;

    ActivityKnowYourCustomerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityComponent(NOSApplication.createActivityComponent(this));
        getActivityComponent().inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_know_your_customer);
        binding.setHandlers(new ClickHandlers());

        presenter.attachView(this);
        presenter.setupSeekbar();
        presenter.openFragmentWithCurrentPosition(0);
    }

    @Override
    public void navigateTo(Fragment fragment) {
        switchToFragment(R.id.kyc_frame_content, fragment, fragment.getTag());
    }

    @Override
    public void startActivity(Class<? extends Activity> klazz) {
        startActivity(new Intent(this, klazz));
        exitScreen();
    }

    @Override
    public void exitScreen() {
        finish();
    }

    @Override
    public void onSeekbarConfigurationChanged(int max) {
        binding.kycSeekbar.setMax(max);
    }

    @Override
    public void updateSeekBarProgress(int progress) {
        ValueAnimator anim = ValueAnimator.ofInt(previousProgressValue, progress)
                .setDuration(600);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addUpdateListener(animation -> {
            Integer intie = (Integer) animation.getAnimatedValue();
            binding.kycSeekbar.setProgress(intie);
        });
        anim.start();
        previousProgressValue = progress;
    }

    public void navigateNext(int position) {
        presenter.navigateNext(position);
    }

    public class ClickHandlers {
        public void onBackClicked(View v) {
            presenter.onBackClicked();
        }
    }
}

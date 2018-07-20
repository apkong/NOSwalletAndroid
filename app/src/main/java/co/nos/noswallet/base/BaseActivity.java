package co.nos.noswallet.base;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import co.nos.noswallet.di.activity.ActivityComponent;
import co.nos.noswallet.di.application.ApplicationComponent;
import co.nos.noswallet.ui.common.ActivityWithComponent;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements ActivityWithComponent {

    private ActivityComponent activityComponent;

    public void setActivityComponent(ActivityComponent activityComponent) {
        this.activityComponent = activityComponent;
    }


    @Override
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    public ApplicationComponent getApplicationComponent() {
        return null;
    }

    public void switchToFragment(int container, Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(container, fragment, tag)
                .commitAllowingStateLoss();
    }
}

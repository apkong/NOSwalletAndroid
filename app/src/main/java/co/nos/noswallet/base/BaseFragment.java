package co.nos.noswallet.base;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import co.nos.noswallet.di.activity.ActivityComponent;

public class BaseFragment<ParentActivity extends BaseActivity> extends Fragment {
@SuppressWarnings("unchecked")
    public ParentActivity getParentActivity() {
        return (ParentActivity) getActivity();
    }

    public boolean canInject() {
        if (getParentActivity() == null) {
            return false;
        } else {
            return getActivityComponent() != null;
        }
    }

    public ActivityComponent getActivityComponent() {
        return getParentActivity().getActivityComponent();
    }

    protected void exitFromHere() {
        if (getActivity() == null) {
            return;
        }
        Context base = getActivity().getBaseContext();
        if (base == null) return;

        Intent i = base.getPackageManager().getLaunchIntentForPackage(base.getPackageName());
        if (i == null) {
            return;
        }
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    protected void showToBeImplementedToast() {
        showToast("To be implemented");
    }

    protected void showToast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }

}

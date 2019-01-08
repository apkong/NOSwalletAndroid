package co.nos.noswallet.ui.send;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import co.nos.noswallet.R;

public class SendButtonUpBehavior extends CoordinatorLayout.Behavior<TextView> {
    public static final String TAG = SendButtonUpBehavior.class.getSimpleName();

    public SendButtonUpBehavior() {
    }

    public SendButtonUpBehavior(Context context) {
        this(context, null);
    }

    public SendButtonUpBehavior(Context context, AttributeSet set) {
        super(context, set);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, TextView child, View dependency) {
        return  dependency instanceof Snackbar.SnackbarLayout && child.getId() == R.id.send_send_button;

    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, TextView child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        Log.d(TAG, "onDependentViewChanged() called with: parent = [" + parent + "], child = [" + child + "], dependency = [" + dependency + "]");

        return false;
    }
}

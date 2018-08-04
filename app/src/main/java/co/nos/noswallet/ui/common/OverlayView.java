package co.nos.noswallet.ui.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class OverlayView extends RelativeLayout {
    public OverlayView(@NonNull Context context) {
        super(context);
        setup(context, null);
    }

    public OverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public OverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setup(Context context, AttributeSet attrs) {
        setOnTouchListener((v, event) -> true);
    }

}

package co.nos.noswallet.ui.common;

import android.os.Parcel;
import android.text.style.URLSpan;
import android.view.View;

import co.nos.noswallet.bus.OpenWebView;
import co.nos.noswallet.bus.RxBus;
import co.nos.noswallet.ui.webview.WebViewDialogFragment;
import co.nos.noswallet.bus.OpenWebView;
import co.nos.noswallet.bus.RxBus;
import co.nos.noswallet.ui.webview.WebViewDialogFragment;

public class CustomUrlSpan extends URLSpan {
    public CustomUrlSpan(String url) {
        super(url);
    }

    public CustomUrlSpan(Parcel src) {
        super(src);
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        if (url != null && widget.getContext() instanceof WindowControl) {
            WebViewDialogFragment
                    .newInstance(url, "")
                    .show(
                            ((WindowControl) widget.getContext()).getFragmentUtility().getFragmentManager(),
                            WebViewDialogFragment.TAG
                    );
        } else if (url != null) {
            RxBus.get().post(new OpenWebView(url));
        }
    }
}

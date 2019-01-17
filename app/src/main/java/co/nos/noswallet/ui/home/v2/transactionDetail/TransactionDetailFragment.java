package co.nos.noswallet.ui.home.v2.transactionDetail;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.R;
import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseDialogFragment;
import co.nos.noswallet.ui.common.WindowControl;
import co.nos.noswallet.util.refundable.Refundable;
import co.nos.noswallet.util.refundable.RefundableBundle;

public class TransactionDetailFragment extends BaseDialogFragment implements TransactionDetailView {

    public static final String CURRENCY = "CURRENCY";
    public static final String ENTRY = "ENTRY";

    public static String TAG = TransactionDetailFragment.class.getSimpleName();

    @Nullable
    private CryptoCurrency cryptoCurrency;

    @Nullable
    private AccountHistory entry;


    private RelativeLayout rootView;
    private TextView exchangeAddress, blockLabel, exchangeStatusLabel, refundButton;

    private ImageView exchangeStatusImage, copyExchangeAddress, blockImage;


    private Handler handler;

    @Inject
    TransactionDetailPresenter presenter;

    public static TransactionDetailFragment newInstance(CryptoCurrency currency,
                                                        AccountHistory entry) {
        Bundle args = new Bundle();
        TransactionDetailFragment fragment = new TransactionDetailFragment();
        args.putSerializable(CURRENCY, currency);
        args.putSerializable(ENTRY, entry);
        fragment.setArguments(args);
        return fragment;
    }

    public static void showFrom(CryptoCurrency cryptoCurrency,
                                AccountHistory entry,
                                FragmentActivity activity) {

        if (activity instanceof WindowControl) {
            TransactionDetailFragment dialog = TransactionDetailFragment.newInstance(cryptoCurrency, entry);
            dialog.show(((WindowControl) activity).getFragmentUtility().getFragmentManager(),
                    TransactionDetailFragment.TAG);

            // make sure that dialog is not null
            ((WindowControl) activity).getFragmentUtility().getFragmentManager().executePendingTransactions();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);
        cryptoCurrency = getSerializableArgument(CURRENCY);
        entry = getSerializableArgument(ENTRY);
        handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        View view = inflater.inflate(R.layout.fragment_transaction_detail, container, false);

        if (entry == null || cryptoCurrency == null) {
            dismiss();
            return view;
        }

        setStatusBarColor(R.color.colorAccent);

        setupToolbar(view);
        rootView = view.findViewById(R.id.rootView);
        refundButton = view.findViewById(R.id.refund_button);
        exchangeAddress = view.findViewById(R.id.exchange_address);
        exchangeStatusLabel = view.findViewById(R.id.amount_transferred_label);
        blockLabel = view.findViewById(R.id.blockhash);
        copyExchangeAddress = view.findViewById(R.id.copy_exchange_address);
        blockImage = view.findViewById(R.id.blockhash_url);
        exchangeStatusImage = view.findViewById(R.id.fragment_transaction_detail_receive_icon);

        presenter.attachView(this);

        presenter.fillWith(entry, cryptoCurrency);

        copyExchangeAddress.setOnClickListener(v -> {
            boolean copied = copyToClipBoard(entry.account);
            if (copied) {
                Snackbar.make(rootView, getString(R.string.address_copied_to_clipboard), Snackbar.LENGTH_LONG).show();
            }
        });

        blockImage.setOnClickListener(v -> {
            presenter.openBlockHashLink(cryptoCurrency, entry.hash);
        });

        return view;
    }

    private boolean copyToClipBoard(String account) {
        Context context = NOSApplication.get();
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.account_address), account);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clip);
            return true;
        }
        return false;
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.dialog_appbar);
        if (toolbar != null) {
            final TransactionDetailFragment window = this;
            TextView title = view.findViewById(R.id.dialog_toolbar_title);
            title.setText(R.string.transaction_details);
            toolbar.setNavigationOnClickListener(v1 -> window.dismiss());
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBlockHashLinkReceived(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        if (i.resolveActivity(NOSApplication.get().getPackageManager()) != null) {
            startActivity(i);
        }
    }

    @Override
    public void onReceiveTransferParams(int drawableRes,
                                        String receiveSendMessage,
                                        boolean shouldShowRefundButton,
                                        String hash,
                                        String address) {

        exchangeAddress.setText(address);
        blockLabel.setText(hash);

        exchangeStatusImage.setImageResource(drawableRes);
        exchangeStatusLabel.setText(receiveSendMessage);

        if (shouldShowRefundButton) {
            refundButton.setVisibility(View.VISIBLE);
            refundButton.setOnClickListener(v -> {
                presenter.attemptRefund(cryptoCurrency, entry);
            });
        } else {
            refundButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void navigateToRefundScreen(RefundableBundle refundableBundle) {
        if (getActivity() instanceof Refundable) {
            ((Refundable) getActivity()).attemptRefund(refundableBundle);
        }
        dismiss();
    }
}

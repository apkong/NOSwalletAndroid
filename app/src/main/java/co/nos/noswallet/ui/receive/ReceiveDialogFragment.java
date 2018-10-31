package co.nos.noswallet.ui.receive;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.sumimakito.awesomeqr.AwesomeQRCode;

import java.io.File;
import java.io.FileOutputStream;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.analytics.AnalyticsEvents;
import co.nos.noswallet.analytics.AnalyticsService;
import co.nos.noswallet.broadcastreceiver.ClipboardAlarmReceiver;
import co.nos.noswallet.databinding.FragmentReceiveBinding;
import co.nos.noswallet.model.Address;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.common.ActivityWithComponent;
import co.nos.noswallet.ui.common.BaseDialogFragment;
import co.nos.noswallet.ui.common.UIUtil;
import io.realm.Realm;

/**
 * Settings main screen
 */
public class ReceiveDialogFragment extends BaseDialogFragment {
    private FragmentReceiveBinding binding;
    public static String TAG = ReceiveDialogFragment.class.getSimpleName();
    private static final int QRCODE_SIZE = 240;

    public static final String CRYPTOCURRENCY = "CRYPTOCURRENCY";

    private static final String TEMP_FILE_NAME = "nanoreceive.png";
    private static final String ADDRESS_KEY = "co.nos.noswallet.ui.receive.ReceiveDialogFragment.Address";
    private Address address;
    private String fileName;

    @Inject
    Realm realm;

    @Inject
    AnalyticsService analyticsService;

    @Nullable
    private CryptoCurrency currency;

    private TextView receiveInstructionsTextView;
    private TextView currencyAddressTextView;

    /**
     * Create new instance of the dialog fragment (handy pattern if any data needs to be passed to it)
     *
     * @return ReceiveDialogFragment instance
     */
    public static ReceiveDialogFragment newInstance() {
        return newInstance(CryptoCurrency.NOLLAR);
    }

    public static ReceiveDialogFragment newInstance(CryptoCurrency cryptoCurrency) {
        Bundle args = new Bundle();
        ReceiveDialogFragment fragment = new ReceiveDialogFragment();
        args.putSerializable(CRYPTOCURRENCY, cryptoCurrency);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);
        currency = getSerializableArgument(CRYPTOCURRENCY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        analyticsService.track(AnalyticsEvents.RECEIVE_VIEWED);

        if (currency == null) {
            currency = CryptoCurrency.NOLLAR;
        }
        // get data
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials != null) {
            address = new Address(credentials.getAddressString(currency), currency);
        }

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_receive, container, false);
        view = binding.getRoot();

        receiveInstructionsTextView = view.findViewById(R.id.receive_instructions);
        currencyAddressTextView = view.findViewById(R.id.receive_address_label);
        receiveInstructionsTextView.setText(getString(R.string.receive_instructions_placeholder, currency.name()));
        currencyAddressTextView.setText(getString(R.string.currency_address_placeholder, currency.name()));
        binding.setHandlers(new ClickHandlers());

        // colorize address text
        if (binding != null &&
                binding.receiveAddress != null &&
                binding.receiveCard != null &&
                binding.receiveCard.cardAddress != null &&
                address != null &&
                address.getAddress() != null) {
            binding.receiveAddress.setText(UIUtil.getColorizedSpannable(address.getAddress(), getContext()));
            binding.receiveCard.cardAddress.setText(UIUtil.getColorizedSpannable(address.getAddress(), getContext()));
        }

        // generate QR code
        new AwesomeQRCode.Renderer()
                .contents(address.getAddress())
                .size((int) UIUtil.convertDpToPixel(QRCODE_SIZE, getContext()))
                .margin((int) UIUtil.convertDpToPixel(20, getContext()))
                .dotScale(0.55f)
                .background(BitmapFactory.decodeResource(getResources(), R.drawable.qrbackground))
                .renderAsync(new AwesomeQRCode.Callback() {
                    @Override
                    public void onRendered(AwesomeQRCode.Renderer renderer, final Bitmap bitmap) {
                        getActivity().runOnUiThread(() -> {
                            binding.receiveBarcode.setImageBitmap(bitmap);
                            binding.receiveCard.cardBarcode.setImageBitmap(bitmap);
                        });
                    }

                    @Override
                    public void onError(AwesomeQRCode.Renderer renderer, Exception e) {
                        e.printStackTrace();
                    }
                });

        setStatusBarWhite(view);

        return view;
    }

    public Bitmap setViewToBitmapImage(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    public void saveImage(Bitmap finalBitmap) {
        try {
            File cachePath = new File(getContext().getCacheDir(), "images");
            cachePath.mkdirs();
            FileOutputStream outputStream;
            fileName = System.currentTimeMillis() + "_" + TEMP_FILE_NAME;
            File file = new File(cachePath + "/" + fileName);
            outputStream = new FileOutputStream(file, true);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ClickHandlers {
        public void onClickClose(View view) {
            dismiss();
        }

        public void onClickShare(View view) {
            analyticsService.track(AnalyticsEvents.SHARE_DIALOGUE_VIEWED);
            binding.receiveCard.cardLayout.setVisibility(View.VISIBLE);
            saveImage(setViewToBitmapImage(binding.receiveCard.cardLayout));
            File imagePath = new File(getContext().getCacheDir(), "images");
            File newFile = new File(imagePath, fileName);
            Uri imageUri = FileProvider.getUriForFile(getContext(), "co.nos.noswallet.fileprovider", newFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_TEXT, address.getAddress());
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setDataAndType(imageUri, getActivity().getContentResolver().getType(imageUri));
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.receive_share_title)));
            binding.receiveCard.cardLayout.setVisibility(View.INVISIBLE);
        }

        public void onClickCopy(View view) {
            analyticsService.track(AnalyticsEvents.NANO_ADDRESS_COPIED);

            // copy address to clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(ClipboardAlarmReceiver.CLIPBOARD_NAME, address.getAddress());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }

            Snackbar snackbar = Snackbar.make(view, Html.fromHtml(getString(R.string.receive_copy_message)), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.receive_copy_done, view1 -> {
            });
            snackbar.show();

            setClearClipboardAlarm();
        }
    }
}

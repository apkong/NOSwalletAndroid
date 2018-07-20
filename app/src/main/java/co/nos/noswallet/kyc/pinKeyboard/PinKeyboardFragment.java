package co.nos.noswallet.kyc.pinKeyboard;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andrognito.pinlockview.PinLockListener;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.databinding.FragmentKyc2PinKeyboardBinding;
import co.nos.noswallet.kyc.KnowYourCustomerActivity;
import co.nos.noswallet.kyc.KycUserDataRepository;

public class PinKeyboardFragment extends co.nos.noswallet.base.BaseFragment<KnowYourCustomerActivity> {

    public static final int POSITION = 2;

    @Inject
    KycUserDataRepository userDataRepository;

    FragmentKyc2PinKeyboardBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // init dependency injection
        if (canInject()) {
            getActivityComponent().inject(this);
        } else {
            exitFromHere();
        }

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_kyc_2_pin_keyboard, container, false);
        View view = binding.getRoot();

        // bind data to view
        binding.setHandlers(new PinKeyboardFragment.ClickHandlers());

        binding.pinLockView.attachIndicatorDots(binding.pinIndicatorDots);

        disableButton();

        binding.pinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                enableButton(pin);
            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                if (pinLength == 4) {
                    enableButton(intermediatePin);
                } else {
                    disableButton();
                }
            }
        });

        return view;
    }

    private void disableButton() {
        binding.kyc1Continue.setBackgroundResource(R.drawable.bg_large_button_gray);
        binding.kyc1Continue.setOnClickListener(null);
    }

    private void enableButton(String pinCode) {
        binding.kyc1Continue.setBackgroundResource(R.drawable.bg_large_button);
        binding.kyc1Continue.setOnClickListener(v -> {
            userDataRepository.pinCode = pinCode;
            getParentActivity().navigateNext(POSITION);
        });
    }

    public class ClickHandlers {

        public void onClickContinue(View view) {

        }
    }

}

package co.nos.noswallet.kyc.smsCode;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.base.BaseFragment;
import co.nos.noswallet.databinding.FragmentKyc3SmsCodeBinding;
import co.nos.noswallet.kyc.KnowYourCustomerActivity;
import co.nos.noswallet.kyc.KycUserDataRepository;

public class SmsCodeFragment extends BaseFragment<KnowYourCustomerActivity> {

    public static final int SEEKBAR_MULTIPLICATOR = 1;

    public static final int POSITION = 3;

    @Inject
    KycUserDataRepository userDataRepository;

    @Inject
    SeekbarUpdater seekbarUpdater;

    FragmentKyc3SmsCodeBinding binding;

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
                inflater, R.layout.fragment_kyc_3_sms_code, container, false);
        View view = binding.getRoot();

        // bind data to view
        binding.setHandlers(new SmsCodeFragment.ClickHandlers());

        binding.kyc3Seekbar.setMax(seekbarUpdater.getTimeout() * SEEKBAR_MULTIPLICATOR);


        enableResendButton(false);
        setupSeekbarUpdates();

        return view;
    }

    private void setupSeekbarUpdates() {
        binding.kyc3Seekbar.setProgress(binding.kyc3Seekbar.getMax());
        seekbarUpdater.startSeekbarTimeout(new SeekbarUpdater.Callbacks() {
            @Override
            public void updateSeekbar(int secondsLeft) {
                binding.kyc3Seekbar.setProgress(secondsLeft * SEEKBAR_MULTIPLICATOR);
                binding.resendInfoLabel.setText(getString(R.string.you_can_resend_sms_code, secondsLeft));
            }

            @Override
            public void onTimeout() {
                enableResendButton(true);
            }
        });
    }

    private void enableResendButton(boolean enable) {
        if (enable) {
            binding.kyc3ResendSmsCode.setBackgroundResource(R.drawable.bg_large_button);
            binding.kyc3ResendSmsCode.setOnClickListener(v -> {
                resendSmsCode();
            });
        } else {
            binding.kyc3ResendSmsCode.setBackgroundResource(R.drawable.bg_large_button_gray);
            binding.kyc3ResendSmsCode.setOnClickListener(null);
        }
    }

    private void resendSmsCode() {
        //todo:
        showToBeImplementedToast();
        setupSeekbarUpdates();
        enableResendButton(false);
    }

    public class ClickHandlers {

        public void onClickContinue(View view) {
            binding.kyc3SmsCodeInput.setError(null);
            if (binding.kyc3SmsCodeInput.getRawText().equalsIgnoreCase("123456")) {
                userDataRepository.smsCode = binding.kyc3SmsCodeInput.getRawText();
                getParentActivity().navigateNext(POSITION);
            } else {
                binding.kyc3SmsCodeInput.setError(getString(R.string.sms_code_mismatch));
            }
        }
    }

    @Override
    public void onDestroyView() {
        seekbarUpdater.destroy();
        super.onDestroyView();
    }
}

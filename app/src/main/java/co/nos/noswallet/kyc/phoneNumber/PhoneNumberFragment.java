package co.nos.noswallet.kyc.phoneNumber;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.databinding.FragmentKyc1PhoneNumberBinding;
import co.nos.noswallet.kyc.KnowYourCustomerActivity;
import co.nos.noswallet.kyc.KycUserDataRepository;

public class PhoneNumberFragment extends co.nos.noswallet.base.BaseFragment<KnowYourCustomerActivity> {

    public static final int POSITION = 1;

    @Inject
    KycUserDataRepository userDataRepository;

    FragmentKyc1PhoneNumberBinding binding;

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
                inflater, R.layout.fragment_kyc_1_phone_number, container, false);
        View view = binding.getRoot();

        // bind data to view
        binding.setHandlers(new PhoneNumberFragment.ClickHandlers());

        binding.kyc1PhoneInput.getPhonePrefixInput().setText(userDataRepository.dialCode);
        binding.kyc1PhoneInput.getPhoneInput().setText(userDataRepository.phoneNumber);

        return view;
    }

    public class ClickHandlers {
        public void onClickGotNewNumber(View view) {
            showToBeImplementedToast();
        }

        public void onClickContinue(View view) {
            binding.kyc1PhoneInput.clearError();
            String dialCode = binding.kyc1PhoneInput.getPhonePrefixInput().getRawText();
            String number = binding.kyc1PhoneInput.getPhoneInput().getTrimmedText();

            if ( number.isEmpty()) {
                String message = getString(R.string.enter_valid_phone_number);
                binding.kyc1PhoneInput.setError(message);
            } else {
                userDataRepository.dialCode = dialCode;
                userDataRepository.phoneNumber = number;
                //navigate next
                getParentActivity().navigateNext(POSITION);
            }
        }

        public void onClickSkip(View view) {
            getParentActivity().finish();
        }
    }

}

package co.nos.noswallet.kyc.emailAddress;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.base.BaseFragment;
import co.nos.noswallet.databinding.FragmentKyc6EmailAddressBinding;
import co.nos.noswallet.kyc.KnowYourCustomerActivity;
import co.nos.noswallet.kyc.KycUserDataRepository;
import co.nos.noswallet.kyc.homeAddress.CountriesRepository;

public class EmailAddressFragment extends BaseFragment<KnowYourCustomerActivity> {

    public static final int POSITION = 6;

    @Inject
    KycUserDataRepository userDataRepository;

    FragmentKyc6EmailAddressBinding binding;

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
                inflater, R.layout.fragment_kyc_6_email_address, container, false);
        View view = binding.getRoot();

        // bind data to view
        binding.setHandlers(new EmailAddressFragment.ClickHandlers());

        return view;
    }

    private void enableButton(boolean enable) {
        binding.kyc1Continue.setBackgroundResource(enable ? R.drawable.bg_large_button : R.drawable.bg_large_button_gray);
    }

    public class ClickHandlers {

        public void onContinue(View view) {
            binding.emailAddressInput.clearError();

            boolean valid = emailValid(binding.emailAddressInput.getTrimmedText());

            if (!valid){
                String emailInvalid = getString(R.string.email_invalid);
                binding.emailAddressInput.setError(emailInvalid);
            }

            enableButton(valid);

            if (valid) {
                userDataRepository.email = binding.emailAddressInput.getTrimmedText();
                getParentActivity().navigateNext(POSITION);
            }
        }
    }

    private boolean emailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}

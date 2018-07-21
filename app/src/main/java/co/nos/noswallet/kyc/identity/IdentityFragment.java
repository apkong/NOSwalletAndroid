package co.nos.noswallet.kyc.identity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.base.SimpleWatcher;
import co.nos.noswallet.databinding.FragmentKyc4IdentityBinding;
import co.nos.noswallet.kyc.KnowYourCustomerActivity;
import co.nos.noswallet.kyc.KycUserDataRepository;

public class IdentityFragment extends co.nos.noswallet.base.BaseFragment<KnowYourCustomerActivity> {

    public static final int POSITION = 4;

    TextWatcher watcher;

    DatePickerDialog datePickerDialog;

    @Inject
    KycUserDataRepository userDataRepository;

    @Inject
    IdentityMapper identityMapper;

    FragmentKyc4IdentityBinding binding;

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
                inflater, R.layout.fragment_kyc_4_identity, container, false);
        View view = binding.getRoot();

        // bind data to view
        binding.setHandlers(new IdentityFragment.ClickHandlers());

        binding.identityFirstName.setText(userDataRepository.firstName);
        binding.identityLastName.setText(userDataRepository.lastName);
        binding.identityBirthdate.setText(userDataRepository.birthDate);

        setupBirthDateListener();

        watcher = new IdentityWatcher();
        binding.identityFirstName.addTextChangedListener(watcher);
        binding.identityLastName.addTextChangedListener(watcher);
        binding.identityBirthdate.addTextChangedListener(watcher);

        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupBirthDateListener() {
        binding.identityBirthdate.setOnEditTextTouchListener((v, event) -> {
            if (datePickerDialog == null || !datePickerDialog.isShowing()) {
                onCalendarPicked();
            }
            return true;
        });
    }

    void enableIdentity() {
        boolean valid = identityMapper.areIdentityValid(
                binding.identityFirstName.getTrimmedText(),
                binding.identityLastName.getTrimmedText(),
                binding.identityBirthdate.getTrimmedText()
        );
        enableButton(valid);
    }

    void onCalendarPicked() {
        DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            Date date = new Date(year - 1900, month, dayOfMonth);
            String dateAsString = identityMapper.formatDate(date);
            binding.identityBirthdate.setText(dateAsString);
        };
        datePickerDialog = new DatePickerDialog(getParentActivity(), listener, 1993, 6, 20);
        datePickerDialog.show();
    }

    private void enableButton(boolean enable) {
        if (enable)
            binding.kyc1Continue.setBackgroundResource(R.drawable.bg_large_button);
        else
            binding.kyc1Continue.setBackgroundResource(R.drawable.bg_large_button_gray);
    }

    public class ClickHandlers {

        public void onClickContinue(View view) {
            binding.identityFirstName.clearError();
            binding.identityLastName.clearError();
            binding.identityBirthdate.clearError();

            boolean canGoNext = true;

            if (binding.identityFirstName.isEmpty()) {
                binding.identityFirstName.setError(getString(R.string.field_cannot_be_blank));
                canGoNext = false;
            }
            if (binding.identityLastName.isEmpty()) {
                binding.identityLastName.setError(getString(R.string.field_cannot_be_blank));
                canGoNext = false;
            }
            if (!identityMapper.isBirthDateValid(binding.identityBirthdate.getTrimmedText())) {
                binding.identityBirthdate.setError(getString(R.string.birthdate_invalid));
                canGoNext = false;
            }

            if (canGoNext) {
                enableButton(true);
            } else {
                return;
            }
            userDataRepository.firstName = binding.identityFirstName.getTrimmedText();
            userDataRepository.lastName = binding.identityLastName.getTrimmedText();
            userDataRepository.birthDate = binding.identityBirthdate.getTrimmedText();

            getParentActivity().navigateNext(POSITION);
        }
    }

    class IdentityWatcher extends SimpleWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            enableIdentity();
        }
    }

}

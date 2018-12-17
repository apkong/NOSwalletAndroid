package co.nos.noswallet.kyc.homeAddress;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.base.BaseFragment;
import co.nos.noswallet.databinding.FragmentKyc5HomeAddressBinding;
import co.nos.noswallet.kyc.KnowYourCustomerActivity;
import co.nos.noswallet.kyc.KycUserDataRepository;

public class HomeAddressFragment extends BaseFragment<KnowYourCustomerActivity> {

    public static final int POSITION = 5;

    @Inject
    KycUserDataRepository userDataRepository;

    @Inject
    CountriesRepository countriesRepository;

    FragmentKyc5HomeAddressBinding binding;

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
                inflater, R.layout.fragment_kyc_5_home_address, container, false);
        View view = binding.getRoot();

        // bind data to view
        binding.setHandlers(new HomeAddressFragment.ClickHandlers());

        setupSpinner();

        return view;
    }

    private void setupSpinner() {
        int entryView = R.layout.item_spinner_entry;
        int dropDownEntryView = R.layout.item_balance_dropdown;
        int offsetDueToHint = 1;

        List<Country> countries = countriesRepository.getCountries();
        countries.add(new Country("", getString(R.string.choose_other_country)));

        ArrayAdapter<Country> adapter = new ArrayAdapter<Country>(getContext(), entryView, countries) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                int realCount = binding.addressCountryPicker.getCount();
                if (position + offsetDueToHint == realCount) {
                    TextView view = (TextView) v;
                    view.setTextColor(Color.BLACK);
                    view.setTypeface(Typeface.DEFAULT_BOLD);
                }
                return v;
            }
        };
        adapter.setDropDownViewResource(dropDownEntryView);
        binding.addressCountryPicker.setAdapter(adapter);
        binding.addressCountryPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            int previousSelection = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int _id = (int) id;
                if (_id == binding.addressCountryPicker.getCount() - 1) {
                    showToBeImplementedToast();
                } else {
                    previousSelection = position + offsetDueToHint;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.addressCountryPicker.setSelection(offsetDueToHint);
    }

    private void enableButton(boolean enable) {
        binding.kyc1Continue.setBackgroundResource(enable ? R.drawable.bg_large_button : R.drawable.bg_large_button_gray);
    }

    public class ClickHandlers {

        public void onContinue(View view) {
            clearAllErrors();

            String emptyError = getString(R.string.please_enter_value);

            boolean valid = true;

            if (binding.addressCountryPicker.getSelectedItem() == null) {
                binding.addressCountryPicker.setError(emptyError);
                valid = false;
            }
            if (binding.addressPostalCode.checkEmptyAndSetError(emptyError)) {
                valid = false;
            }
            if (binding.addressPart1.checkEmptyAndSetError(emptyError)) {
                valid = false;
            }

            if (binding.addressStreet.checkEmptyAndSetError(emptyError)) {
                valid = false;
            }
            if (binding.addressRegion.checkEmptyAndSetError(emptyError)) {
                valid = false;
            }

            enableButton(valid);

            if (valid) {
                userDataRepository.addressPart1 = binding.addressPart1.getTrimmedText();
                userDataRepository.addressPart2 = binding.addressPart2.getTrimmedText();
                userDataRepository.postalCode = binding.addressPostalCode.getTrimmedText();
                userDataRepository.street = binding.addressStreet.getTrimmedText();
                userDataRepository.region = binding.addressRegion.getTrimmedText();
                userDataRepository.country = (Country) binding.addressCountryPicker.getSelectedItem();

                getParentActivity().navigateNext(POSITION);
            }
        }
    }

    private void clearAllErrors() {
        binding.addressPart1.clearError();
        binding.addressPart2.clearError();
        binding.addressPostalCode.clearError();
        binding.addressCountryPicker.clearError();
        binding.addressStreet.clearError();
        binding.addressRegion.clearError();
    }

}

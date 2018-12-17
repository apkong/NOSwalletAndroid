package co.nos.noswallet.kyc.done;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.base.BaseFragment;
import co.nos.noswallet.databinding.FragmentKyc6EmailAddressBinding;
import co.nos.noswallet.databinding.FragmentKyc7DoneBinding;
import co.nos.noswallet.kyc.KnowYourCustomerActivity;
import co.nos.noswallet.kyc.KycUserDataRepository;

public class DoneFragment extends BaseFragment<KnowYourCustomerActivity> {

    public static final int POSITION = 7;

    @Inject
    KycUserDataRepository userDataRepository;

    FragmentKyc7DoneBinding binding;

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
                inflater, R.layout.fragment_kyc_7_done, container, false);
        View view = binding.getRoot();

        // bind data to view
        binding.setHandlers(new DoneFragment.ClickHandlers());

        return view;
    }

    public class ClickHandlers {

        public void onDone(View view) {
            getParentActivity().navigateNext(POSITION);
        }
    }


}

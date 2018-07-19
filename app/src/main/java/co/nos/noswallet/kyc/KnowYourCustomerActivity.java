package co.nos.noswallet.kyc;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import co.nos.noswallet.R;
import co.nos.noswallet.databinding.ActivityKnowYourCustomerBinding;

public class KnowYourCustomerActivity extends AppCompatActivity {
    public static final String TAG = "KYCActivity";

    ActivityKnowYourCustomerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_know_your_customer);

    }

    public class ClickHandlers {
        public void onBackClicked(View v) {

            Log.d(TAG, "onBackClicked: v");

        }

        public void onBackClicked() {
            Log.d(TAG, "onBackClicked: ");
        }
    }
}

package com.eserve.android.bottomSheets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.eserve.android.R;
import com.eserve.android.customer.activity.CustomerMainActivity;
import com.eserve.android.databinding.SheetRateExperienceBinding;
import com.eserve.android.model.Navigation;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.Constants;
import com.eserve.android.utils.FirebaseRef;
import com.eserve.android.utils.SharedPrefManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;
import java.util.Map;


public class RateExperienceSheet extends BottomSheetDialogFragment {

    public static final String TAG = "RateExperienceSheet";

    //    Context
    private FragmentActivity mContext;

    //    Data binding
    private SheetRateExperienceBinding binding;

    // Model class
    private Navigation navigation;

    //    Variables
    private float rating = 0;


    public RateExperienceSheet() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.ServiceProviderInfoBottomSheetTheme);

        mContext = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.sheet_rate_experience, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Get navigation data
        navigation = ApplicationUtils.getNavigation(mContext);

        // Click listeners
        binding.done.setOnClickListener(v -> validateInputFields());
        binding.payment.setOnClickListener(v ->
                Toast.makeText(mContext, "Coming soon", Toast.LENGTH_SHORT).show()
        );

        // Ratings changed listener
        binding.ratings.setOnRatingChangeListener((ratingBar, ratingStar) -> rating = ratingStar);

    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/


    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/


    private void validateInputFields() {
        String text = binding.ratingMessage.getText().toString().trim();

        if (rating != 0) {

            saveRateExperience(rating, text);

        } else {
            Toast.makeText(mContext, "Rating required", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRateExperience(float rating, String text) {

        String servicerId = navigation.getSent_by();
        String customerId = navigation.getSent_to();
        String ratingId = FirebaseRef.getRatingRef().child(servicerId).push().getKey();

        Map<String, Object> map = new HashMap<>();

        map.put("ratings/" + servicerId + "/" + ratingId + "/rating", rating);
        map.put("ratings/" + servicerId + "/" + ratingId + "/text", text);

        // Update message request status
        map.put("requests/" + servicerId + "/" + navigation.getRequest_id() + "/status", 2); // status = 2 --> navigation complete

        // Delete navigation
        map.put("navigation/" + customerId, null);

        // Delete chat
        map.put("messages/" + customerId, null);
        map.put("messages/" + servicerId, null);


        assert ratingId != null;
        FirebaseRef.getDatabaseInstance()
                .updateChildren(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
                        prefManager.storeSharedData(Constants.IS_NAVIGATION_RUNNING, false);
                        prefManager.storeSharedData(Constants.IS_RATE_EXPERIENCE_READY, false);
                        prefManager.clearString(Constants.CURRENT_NAVIGATION);

                        Toast.makeText(mContext, "you rated successfully!", Toast.LENGTH_SHORT).show();

                        // Start Service provider main activity
                        startActivity(new Intent(mContext, CustomerMainActivity.class));
                        mContext.finish();

                    } else {
                        Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
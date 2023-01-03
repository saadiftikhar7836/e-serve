package com.eserve.android.serviceProvider.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.eserve.android.R;
import com.eserve.android.databinding.ActivityServiceEditProfileBinding;
import com.eserve.android.enums.Role;
import com.eserve.android.model.User;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.CheckEmptyFields;
import com.eserve.android.utils.FirebaseRef;
import com.eserve.android.utils.ProgressDialog;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ServiceEditProfileActivity extends AppCompatActivity {

    // Context
    private AppCompatActivity mContext;

    // Data binding
    private ActivityServiceEditProfileBinding binding;

    // Model class
    private User serviceProvider;

    // Variables
    private String businessName,
            address,
            placeId;
    private Double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        binding = DataBindingUtil.setContentView(mContext, R.layout.activity_service_edit_profile);


        // Action bar
        setSupportActionBar(binding.toolbar);
        setTitle(R.string.label_edit_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Initialize
        initialization();


        // Get current data
        setUserData();


        // Click listener
        binding.searchLocation.setOnClickListener(v -> searchAddress());
        binding.updateBtn.setOnClickListener(v -> updateData());

    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);

    }


    ActivityResultLauncher<Intent> autoCompleteAddressLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                assert result.getData() != null;
                if (result.getResultCode() == RESULT_OK) {

                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    placeId = place.getId();
                    address = place.getAddress();
                    latitude = Objects.requireNonNull(place.getLatLng()).latitude;
                    longitude = Objects.requireNonNull(place.getLatLng()).longitude;

                    // Set address
                    binding.address.setText(address);

                    Log.i("Search_Place", "Place: " + place.getLatLng() + ", " + place.getName() + ", " + place.getAddress() + ", " + place.getId());

                }
            });


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    private void initialization() {

        //        Google places
        if (!Places.isInitialized()) {
            Places.initialize(
                    mContext.getApplicationContext(),
                    mContext.getString(R.string.google_maps_api_key)
            );
        }

    }

    private void searchAddress() {

        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.OPENING_HOURS);


        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("PK")
                .build(mContext);
        autoCompleteAddressLauncher.launch(intent);

    }


    private void setUserData() {

        // Get data
        serviceProvider = ApplicationUtils.getUserDetail(mContext);

        // Set hint
        if (serviceProvider.getRole().equals(Role.MECHANIC.toString().toLowerCase()))
            binding.etBusinessName.setHint(mContext.getString(R.string.service_station_name));
        else if (serviceProvider.getRole().equals(Role.MECHANIC.toString().toLowerCase()))
            binding.etBusinessName.setHint(mContext.getString(R.string.clinic_name));

        // Set service provider data
        binding.etBusinessName.setText(serviceProvider.getBusiness_name());
        binding.fullName.setText(serviceProvider.getFull_name());
        binding.phoneNo.setText(serviceProvider.getPhone_no());
        binding.address.setText(serviceProvider.getLocation().getAddress());

    }

    private void updateData() {
        businessName = binding.etBusinessName.getText().toString().trim();
        String fullname = binding.fullName.getText().toString().trim();
//        String countryCode = Sp_Country_Code.getSelectedCountryCode();
        String phoneNo = binding.phoneNo.getText().toString().trim();
//        String phone = countryCode + phoneNo;

        if (CheckEmptyFields.isEditText(mContext, fullname, binding.fullName) &&
                CheckEmptyFields.isEditText(mContext, phoneNo, binding.phoneNo)) {

            ProgressDialog.show(mContext, 0, R.string.label_update);

            Map<String, Object> map = new HashMap<>();

            if (!TextUtils.isEmpty(address)) {

                Map<String, Object> locationMap = new HashMap<>();

                // Location map
                locationMap.put("place_id", placeId);
                locationMap.put("address", address);
                locationMap.put("lat", latitude);
                locationMap.put("lng", longitude);
                map.put("location", locationMap);

            }

            map.put("business_name", businessName);
            map.put("full_name", fullname);
            map.put("phone_no", phoneNo);


            FirebaseRef.getUserRef().child(FirebaseRef.getUserId()).updateChildren(map)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            Toast.makeText(mContext, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            ProgressDialog.dismiss();
                            finish();

                        } else {
                            Toast.makeText(mContext, "Alert!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }


}
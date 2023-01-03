package com.eserve.android.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.eserve.android.R;
import com.eserve.android.databinding.ActivitySignupBinding;
import com.eserve.android.enums.Role;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.CheckEmptyFields;
import com.eserve.android.utils.FirebaseRef;
import com.eserve.android.utils.ProgressDialog;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.widget.Toast.LENGTH_SHORT;

public class SignUpActivity extends AppCompatActivity {

    //    Context
    private AppCompatActivity mContext;

    //    Data binding
    private ActivitySignupBinding binding;


    //   Variables
    private String fullName = "",
            email = "",
            role = "",
            address,
            placeId,
            businessName,
            phoneNo = "",
            password = "",
            confirmPass,
            countryCode = "";


    private boolean isInputFields = false, isPasswordVisible = false, isConfirmPasswordVisible = false, isValidPhoneNumber = false;
    private Double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        binding = DataBindingUtil.setContentView(mContext, R.layout.activity_signup);

        initialization();


        // Spinner user role
        spinnerUserRole();


        // Change listener
        textChangeListener();

        //       Click listeners
        binding.clRoot.setOnClickListener(v -> ApplicationUtils.hideKeyboard(mContext));
        binding.backPress.setOnClickListener(v -> finish());
        binding.searchLocation.setOnClickListener(v -> searchAddress());
        binding.btnSignUp.setOnClickListener(v -> getInputFromUser());
        binding.llAlreadyAccount.setOnClickListener(v -> finish());
        binding.cnfrmPassHide.setOnClickListener(this::confirmPasswordVisibility);
        binding.passHide.setOnClickListener(this::passwordVisibility);


    }

    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

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
     *                                                                       Calling methods
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

    private void textChangeListener() {
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                emailValid(s);
            }
        });
    }

    private void emailValid(Editable s) {
        if (!TextUtils.isEmpty(s)) {
            if (!ApplicationUtils.isValidEmail(s.toString())) {
                binding.etEmail.requestFocus();
                binding.etEmail.setError("invalid format");
            }
        }
    }

    private void spinnerUserRole() {

//       Set 1st item disable
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, getResources().getStringArray(R.array.user_role)) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the 1st item from Spinner
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the disable item text color
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };


        roleAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        binding.spRole.setAdapter(roleAdapter);
        binding.spRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                role = adapterView.getItemAtPosition(i).toString();

                if (role.equals(mContext.getString(R.string.label_role)))
                    role = "";
                else if (role.equals(Role.CUSTOMER.toString().toLowerCase())) {

                    binding.etBusinessName.setVisibility(View.GONE);
                    binding.llChooseLocation.setVisibility(View.GONE);


                } else if (role.equals(Role.MECHANIC.toString().toLowerCase())) {

                    binding.etBusinessName.setHint(mContext.getString(R.string.service_station_name));

                    binding.etBusinessName.setVisibility(View.VISIBLE);
                    binding.llChooseLocation.setVisibility(View.VISIBLE);

                } else if (role.equals(Role.RESCUE.toString().toLowerCase())) {

                    binding.etBusinessName.setHint(mContext.getString(R.string.clinic_name));

                    binding.etBusinessName.setVisibility(View.VISIBLE);
                    binding.llChooseLocation.setVisibility(View.VISIBLE);

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    private void getInputFromUser() {

        fullName = binding.etFullName.getText().toString().trim();
        businessName = binding.etBusinessName.getText().toString().trim();
        email = binding.etEmail.getText().toString().trim();
        countryCode = binding.spCountryCode.getSelectedCountryCodeWithPlus();
        phoneNo = binding.etPhoneNo.getText().toString().trim();
        password = binding.etPassword.getText().toString().trim();
        confirmPass = binding.etConfirmPassword.getText().toString().trim();

        int phoneNoLength = phoneNo.length();

        isValidPhoneNumber = phoneNoLength == 10;

        // Check phone no valid or not
        if(!isValidPhoneNumber)
        {
            Toast.makeText(mContext, "Please enter a phone number of length 10", Toast.LENGTH_SHORT).show();
//            binding.etPhoneNo.setFocus
        }



        // Check input is empty or not
        if (isCommonFields()) {
            if (role.equals(Role.CUSTOMER.toString().toLowerCase())) {
                isInputFields = true;
            } else if (role.equals(Role.MECHANIC.toString().toLowerCase()) ||
                    role.equals(Role.RESCUE.toString().toLowerCase())) {
                if (CheckEmptyFields.isEditText(mContext, businessName, binding.etBusinessName) &&
                        !TextUtils.isEmpty(address)) {
                    isInputFields = true;
                } else if (TextUtils.isEmpty(address)) {
                    isInputFields = false;
                    Toast.makeText(mContext, "Please select your location", Toast.LENGTH_SHORT).show();
                }
            }


        } else if (TextUtils.isEmpty(role)) {
            isInputFields = false;
            Toast.makeText(mContext, "Please select your role", Toast.LENGTH_SHORT).show();
        } else isInputFields = false;

        // If all condition satisfied
        if (isInputFields) {

            // Hide keyboard
            ApplicationUtils.hideKeyboard(mContext);


            // Show progress dialog
            ProgressDialog.show(mContext, R.string.creating_acc,
                    R.string.plz_w8);

            // User creating
            createUser(email, password);

        }

    }

    private boolean isCommonFields() {
        return CheckEmptyFields.isEditText(mContext, fullName, binding.etFullName) &&
                CheckEmptyFields.isEditText(mContext, email, binding.etEmail) &&
                CheckEmptyFields.isEditText(mContext, phoneNo, binding.etPhoneNo) &&
                !TextUtils.isEmpty(role) &&
                CheckEmptyFields.isEditText(mContext, password, binding.etPassword) &&
                CheckEmptyFields.isEditText(mContext, confirmPass, binding.etConfirmPassword) &&
                CheckEmptyFields.isPassMatch(mContext, password, confirmPass, binding.etConfirmPassword);
    }

    private void createUser(String email, String password) {
        FirebaseRef.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {


                        // Get user & send verification mail
                        FirebaseUser user = FirebaseRef.getCurrentUser();
                        user.sendEmailVerification();


                        // User data saved to firebase database
                        saveInfoToFirebase();


                    } else {

                        // Dismiss dialog
                        ProgressDialog.dismiss();

                        String error = task.getException().getMessage();
                        Toast.makeText(mContext, "Error: " + error, LENGTH_SHORT).show();
                    }
                });

    }

    private void saveInfoToFirebase() {
        String userId = FirebaseRef.getUserId();

        Map<String, Object> userMap = new HashMap<>();

        // Location map
        if (!TextUtils.isEmpty(address)) {

            Map<String, Object> locationMap = new HashMap<>();

            // Location map
            locationMap.put("place_id", placeId);
            locationMap.put("address", address);
            locationMap.put("lat", latitude);
            locationMap.put("lng", longitude);
            userMap.put("location", locationMap);
            userMap.put("business_name", businessName);
            userMap.put("is_available", 1);

        }

        // User map
        userMap.put("full_name", fullName);
        userMap.put("email", email);
        userMap.put("profile_image", ApplicationUtils.DEFAULT_IMAGE);
        userMap.put("role", role);
        userMap.put("phone_no", countryCode + phoneNo);
        userMap.put("password", password);


        FirebaseRef.getUserRef().child(userId).updateChildren(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Dismiss dialog
                        ProgressDialog.dismiss();


                        Toast.makeText(mContext, getString(R.string.email_verify_msg), Toast.LENGTH_SHORT).show();

                        FirebaseRef.getAuth().signOut();

                        Intent intent = new Intent(mContext, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {

                        // Dismiss dialog
                        ProgressDialog.dismiss();
                        Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void confirmPasswordVisibility(View view) {
        if (isConfirmPasswordVisible) {
            isConfirmPasswordVisible = false;
            binding.cnfrmPassHide.setImageResource(R.drawable.ic_hide);
            binding.etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        } else {
            isConfirmPasswordVisible = true;
            binding.cnfrmPassHide.setImageResource(R.drawable.ic_eye);
            binding.etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
    }

    private void passwordVisibility(View view) {

        if (isPasswordVisible) {
            isPasswordVisible = false;
            binding.passHide.setImageResource(R.drawable.ic_hide);
            binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        } else {
            isPasswordVisible = true;
            binding.passHide.setImageResource(R.drawable.ic_eye);
            binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
    }


}
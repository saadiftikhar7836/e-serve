package com.eserve.android.customer.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.eserve.android.R;
import com.eserve.android.databinding.ActivityCustomerEditProfileBinding;
import com.eserve.android.model.User;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.CheckEmptyFields;
import com.eserve.android.utils.FirebaseRef;
import com.eserve.android.utils.ProgressDialog;

import java.util.HashMap;
import java.util.Map;

public class CustomerEditProfileActivity extends AppCompatActivity {

    // Context
    private AppCompatActivity mContext;

    // Data binding
    private ActivityCustomerEditProfileBinding binding;

    // Model class
    private User user;

    // Variables
    private String address = "",
            placeId;
    private Double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        binding = DataBindingUtil.setContentView(mContext, R.layout.activity_customer_edit_profile);


        // Action bar
        setSupportActionBar(binding.toolbar);
        setTitle(R.string.label_edit_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


//       Get current data
        setUserData();


        // Click listener
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


    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/


    private void setUserData() {
        user = ApplicationUtils.getUserDetail(mContext);

        binding.fullName.setText(user.getFull_name());
        binding.phoneNo.setText(user.getPhone_no());
    }

    private void updateData() {
        String fullname = binding.fullName.getText().toString().trim();
//        String countryCode = Sp_Country_Code.getSelectedCountryCode();
        String phoneNo = binding.phoneNo.getText().toString().trim();
//        String phone = countryCode + phoneNo;

        if (CheckEmptyFields.isEditText(mContext, fullname, binding.fullName) &&
                CheckEmptyFields.isEditText(mContext, phoneNo, binding.phoneNo)) {

            ProgressDialog.show(mContext, 0, R.string.label_update);

            Map<String, Object> map = new HashMap<>();

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
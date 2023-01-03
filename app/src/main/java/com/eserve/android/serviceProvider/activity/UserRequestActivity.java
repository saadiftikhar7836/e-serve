package com.eserve.android.serviceProvider.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.eserve.android.R;
import com.eserve.android.activity.ChatRoomActivity;
import com.eserve.android.databinding.ActivityUserRequestBinding;
import com.eserve.android.model.MessageRequest;
import com.eserve.android.model.User;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.Constants;
import com.eserve.android.utils.FirebaseRef;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UserRequestActivity extends AppCompatActivity {

    // Context
    AppCompatActivity mContext;

    // Data binding
    ActivityUserRequestBinding binding;

    // Model class
    MessageRequest messageRequest;
    User requester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        binding = DataBindingUtil.setContentView(mContext, R.layout.activity_user_request);

        // Set toolbar
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(mContext.getString(R.string.label_request));


        // Get message request
        messageRequest = ApplicationUtils.getUserRequest(mContext);

        // Get requester data
        getRequesterData();


        // Click listener
        binding.call.setOnClickListener(v -> makeCall());
        binding.chat.setOnClickListener(v -> startChat());

    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);

    }

    ActivityResultLauncher<String> callPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Log.e("GALLERY_PERMISSION", "onActivityResult: PERMISSION GRANTED");
                } else {
                    Log.e("GALLERY_PERMISSION", "onActivityResult: PERMISSION DENIED");
                }
            });


    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/

    private void getRequesterData() {
        FirebaseRef.getUserRef().child(messageRequest.getSent_by())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            requester = snapshot.getValue(User.class);

                            assert requester != null;
                            setData();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Log.d("User_Fail", "onCancelled: ");
                    }
                });
    }

    private void setData() {

        Glide.with(mContext.getApplicationContext()).load(requester.getProfile_image()).into(binding.profileImage);
        binding.fullName.setText(requester.getFull_name());
        binding.address.setText(messageRequest.getAddress());

    }


    private void makeCall() {
        String phoneNo = requester.getPhone_no();

        if (!TextUtils.isEmpty(phoneNo)) {

            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                callPermissionResult.launch(Manifest.permission.CALL_PHONE);

            } else {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNo));
                startActivity(Intent.createChooser(callIntent, "Make call..."));

            }

        } else
            noPhoneNo();
    }

    private void noPhoneNo() {
        Toast.makeText(mContext, "No Phone no available", Toast.LENGTH_SHORT).show();
    }

    private void startChat() {
        Intent intent = new Intent(mContext, ChatRoomActivity.class);
        intent.putExtra(Constants.USER_ID, messageRequest.getSent_by());
        mContext.startActivity(intent);
    }


}
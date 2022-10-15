package com.codesses.e_serve.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.codesses.e_serve.R;
import com.codesses.e_serve.auth.LoginActivity;
import com.codesses.e_serve.customer.activity.CustomerMainActivity;
import com.codesses.e_serve.enums.Role;
import com.codesses.e_serve.serviceProvider.activity.ServiceMainActivity;
import com.codesses.e_serve.model.ServiceLocation;
import com.codesses.e_serve.model.User;
import com.codesses.e_serve.utils.ApplicationUtils;
import com.codesses.e_serve.utils.FirebaseRef;
import com.codesses.e_serve.utils.SharedPrefManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public class SplashActivity extends AppCompatActivity {

    //    Context
    private AppCompatActivity mContext;

    //    Intent
    private Intent intent = null;

    //    Handler
    private Handler handler;
    private Runnable runnable;

    //    Firebase
    DatabaseReference userRef;
    ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;


        handler = new Handler();

//      Hide status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        if (FirebaseRef.getCurrentUser() != null) {

            userRef = FirebaseRef.getUserRef().child(FirebaseRef.getUserId());

            saveFcmToken();

        } else {

            intent = new Intent(mContext, LoginActivity.class);

            runnable = () -> {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            };

            handler.postDelayed(runnable, 2000);

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

//        Remove event listener to prevent screen backward
//        when update current user data
//        Note: use single event listener instead of value event listener
        if (FirebaseRef.getCurrentUser() != null)
            userRef.removeEventListener(valueEventListener);

    }

    private void saveFcmToken() {
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(mContext);


        Map<String, Object> map = new HashMap<>();

        map.put("fcm_token", sharedPrefManager.getSharedData(mContext.getString(R.string.intent_fcm_token)));

        FirebaseRef.getUserRef().child(FirebaseRef.getUserId()).updateChildren(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        getCurrentUserData();

                    } else {

                        Log.e(ApplicationUtils.ERROR_TAG, task.getException().getMessage());

                    }
                });
    }

    private void getCurrentUserData() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    assert user != null;
                    user.setUserId(dataSnapshot.getRef().getKey());
                    ServiceLocation location = dataSnapshot.child("location").getValue(ServiceLocation.class);
                    user.setLocation(location);

                    // Data save to preference
                    ApplicationUtils.saveUserToPreference(mContext, user);


                    if (user.getRole().equals(Role.CUSTOMER.toString().toLowerCase())) {

                        intent = new Intent(mContext, CustomerMainActivity.class);

                    } else if (user.getRole().equals(Role.MECHANIC.toString().toLowerCase()) ||
                            user.getRole().equals(Role.RESCUE.toString().toLowerCase())) {

                        intent = new Intent(mContext, ServiceMainActivity.class);

                    }


                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {
                Log.e(ApplicationUtils.ERROR_TAG, databaseError.getMessage());
            }
        };

        userRef.addValueEventListener(valueEventListener);


    }


}
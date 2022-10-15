package com.codesses.e_serve.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.codesses.e_serve.R;
import com.codesses.e_serve.customer.activity.CustomerMainActivity;
import com.codesses.e_serve.databinding.ActivityLoginBinding;
import com.codesses.e_serve.enums.Role;
import com.codesses.e_serve.interfaces.OnForgotPassClick;
import com.codesses.e_serve.serviceProvider.activity.ServiceMainActivity;
import com.codesses.e_serve.model.ServiceLocation;
import com.codesses.e_serve.model.User;
import com.codesses.e_serve.utils.ApplicationUtils;
import com.codesses.e_serve.utils.FirebaseRef;
import com.codesses.e_serve.utils.ProgressDialog;
import com.codesses.e_serve.utils.SharedPrefManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements OnForgotPassClick {


    //    Context
    private AppCompatActivity mContext;

    //    Data binding
    private ActivityLoginBinding binding;

    //    Variables
    private boolean isEmail = false;
    private boolean isPassword = false;
    private boolean isPasswordVisible = false;


    //    Firebase
    DatabaseReference userRef;
    ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        binding = DataBindingUtil.setContentView(mContext, R.layout.activity_login);


        //        Click listeners
        binding.clRoot.setOnClickListener(v -> ApplicationUtils.hideKeyboard(mContext));
        binding.forgotPass.setOnClickListener(v -> forgotPassword());
        binding.btnLoginIn.setOnClickListener(v -> login());
        binding.llCreateAccount.setOnClickListener(v -> openSignUpActivity());
        binding.passHide.setOnClickListener(this::passwordVisibility);


    }

    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/


    @Override
    public void onApply(String email) {

        FirebaseRef.getAuth().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(mContext, mContext.getString(R.string.check_email), Toast.LENGTH_SHORT).show();
                    } else {
                        String error = task.getException().toString();
                        Log.d("ERROR: ", "Error: " + error);
                        Toast.makeText(mContext, mContext.getString(R.string.no_user_found_against_email), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        Remove event listener to prevent screen backward
//        when update current user data
        if (FirebaseRef.getCurrentUser() != null)
            userRef.removeEventListener(valueEventListener);

    }


    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/


    private void login() {

        String email = binding.etEmail.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();

//      heck input iss empty or not
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {

            ApplicationUtils.hideKeyboard(mContext);

//          Show progress dialog
            ProgressDialog.show(mContext,
                    R.string.signing_in,
                    R.string.plz_w8);


//           Login with email
            signInWithEmail(email, pass);
        }
    }

    private void signInWithEmail(String email, String password) {
        FirebaseRef.getAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = FirebaseRef.getCurrentUser();

                        if (user.isEmailVerified()) {

//                            Get current user information
                            getCurrentUserInfo(user.getUid(), password);

                        } else {
                            user.sendEmailVerification();

//                            Dismiss dialog
                            ProgressDialog.dismiss();


                            Toast.makeText(mContext, mContext.getString(R.string.email_verify_msg), Toast.LENGTH_SHORT).show();

//                            SignOut for email verification
                            FirebaseRef.getAuth().signOut();
                        }
                    } else {

                        //            TODO: DISMISS DIALOG
                        ProgressDialog.dismiss();
                        Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void getCurrentUserInfo(String uid, String password) {

        userRef = FirebaseRef.getUserRef().child(uid);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    assert user != null;
                    user.setUserId(uid);
                    ServiceLocation location = dataSnapshot.child("location").getValue(ServiceLocation.class);
                    user.setLocation(location);

                    // User save to local storage
                    ApplicationUtils.saveUserToPreference(mContext, user);

                    if (user.getRole().equals(Role.CUSTOMER.toString().toLowerCase())) {

                        updateCurrentUserPassword(CustomerMainActivity.class, password);

                    } else if (user.getRole().equals(Role.MECHANIC.toString().toLowerCase()) ||
                            user.getRole().equals(Role.RESCUE.toString().toLowerCase())) {

                        updateCurrentUserPassword(ServiceMainActivity.class, password);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {
                Log.e(ApplicationUtils.ERROR_TAG, databaseError.getMessage());
            }
        };

        userRef.addValueEventListener(valueEventListener);
    }

    private void updateCurrentUserPassword(Class activity, String password) {

        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);

        Map<String, Object> map = new HashMap<>();

        map.put("password", password);
        map.put("fcm_token", prefManager.getSharedData(mContext.getString(R.string.intent_fcm_token)));

        userRef.removeEventListener(valueEventListener);


        FirebaseRef.getUserRef().child(FirebaseRef.getUserId())
                .updateChildren(map)
                .addOnCompleteListener(task -> {
                    if ((task.isSuccessful())) {

                        Intent intent = new Intent(mContext, activity);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();


                        // Dismiss dialog
                        ProgressDialog.dismiss();

                    }
                });

    }


    private void forgotPassword() {

        ForgotPassDialog forgotPassDialog = new ForgotPassDialog();
        forgotPassDialog.setCancelable(false);
        forgotPassDialog.show(getSupportFragmentManager(), ForgotPassDialog.TAG);
    }

    private void openSignUpActivity() {
        startActivity(new Intent(mContext, SignUpActivity.class));
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
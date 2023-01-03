package com.eserve.android.serviceProvider.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.eserve.android.R;
import com.eserve.android.activity.AboutUsActivity;
import com.eserve.android.auth.LoginActivity;
import com.eserve.android.databinding.FragmentServiceMenuBinding;
import com.eserve.android.dialogs.ChangePassDialog;
import com.eserve.android.model.User;
import com.eserve.android.serviceProvider.activity.ServiceEditProfileActivity;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.FirebaseRef;
import com.eserve.android.utils.ProgressDialog;
import com.eserve.android.utils.SharedPrefManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ServiceMenuFragment extends Fragment {

    // Context
    private FragmentActivity mContext;

    // Data binding
    private FragmentServiceMenuBinding binding;


    // Model class
    private User serviceProvider;

    // Variables
    private Boolean currentAvailability = false;

    public ServiceMenuFragment() {
        // Required empty public constructor
    }

    public static ServiceMenuFragment newInstance(String param1, String param2) {
        ServiceMenuFragment fragment = new ServiceMenuFragment();
        Bundle args = new Bundle();
        args.putString("ARG_PARAM1", param1);
        args.putString("ARG_PARAM2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_menu, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get current user
        serviceProvider = ApplicationUtils.getUserDetail(mContext);

        // Set current user data
        setCurrentUserData();

        // Switch change listener
        binding.availability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) updateStatus(1);
            else updateStatus(0);
        });

        // Click listener
        binding.editProfile.setOnClickListener(v -> openEditProfile());
        binding.add.setOnClickListener(v -> galleryPermission());
        binding.changePass.setOnClickListener(v -> openChangePassDialog());
        binding.contactUs.setOnClickListener(v -> makeCall());
        binding.aboutUs.setOnClickListener(v -> openAboutActivity());
        binding.clLogout.setOnClickListener(v -> userLogOut());

    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    ActivityResultLauncher<String> galleryPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Log.e("GALLERY_PERMISSION", "onActivityResult: PERMISSION GRANTED");
                } else {
                    Log.e("GALLERY_PERMISSION", "onActivityResult: PERMISSION DENIED");
                }
            });

    ActivityResultLauncher<String> callPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Log.e("CALL_PERMISSION", "onActivityResult: PERMISSION GRANTED");
                } else {
                    Log.e("CALL_PERMISSION", "onActivityResult: PERMISSION DENIED");
                }
            });

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    // There are no request codes
                    Intent data = result.getData();

                    assert data != null;
                    binding.profileImg.setImageURI(data.getData());
                    uploadToStorage(data.getData());
                    ProgressDialog.show(mContext, R.string.title_profile, R.string.uploading);

                }
            });


    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/

    private void setCurrentUserData() {

        Glide.with(mContext.getApplicationContext())
                .load(serviceProvider.getProfile_image()).into(binding.profileImg);
        binding.businessName.setText(serviceProvider.getBusiness_name());
        binding.fullName.setText(serviceProvider.getFull_name());
        binding.email.setText(serviceProvider.getEmail());
        binding.phoneNo.setText(serviceProvider.getPhone_no());
        binding.role.setText(serviceProvider.getRole());

        if (serviceProvider.getIs_available() == 0)
            currentAvailability = false;
        else if (serviceProvider.getIs_available() == 1)
            currentAvailability = true;

        binding.availability.setChecked(currentAvailability);

    }

    private void galleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {

                galleryPermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

            } else galleryImagePick();

        } else galleryImagePick();

    }

    private void galleryImagePick() {

        Intent intent = new Intent();

        intent.setAction(Intent.ACTION_PICK);

        intent.setType("image/*");
        someActivityResultLauncher.launch(intent);

    }


    private void userLogOut() {
        new AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.label_signout_surety))
                .setNegativeButton(mContext.getText(R.string.label_no), null)
                .setPositiveButton(mContext.getText(R.string.label_yes), (arg0, arg1) ->
                        removeFcmToken())
                .create().show();
    }

    private void removeFcmToken() {
        Map<String, Object> map = new HashMap<>();

        map.put("fcm_token", "");

        FirebaseRef.getUserRef().child(serviceProvider.getUserId()).updateChildren(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseRef.getAuth().signOut();
                        Intent intent = new Intent(mContext, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mContext.finish();
                        startActivity(intent);
                    }
                });
    }


    private void uploadToStorage(Uri data) {
        StorageReference reference = FirebaseRef.getProfileStorage().child(FirebaseRef.getUserId());
        UploadTask uploadTask = reference.putFile(data);

        uploadTask.addOnProgressListener(taskSnapshot -> {
            // do nothing
        }).addOnSuccessListener(taskSnapshot ->
                reference.getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        profileUpload(task.getResult().toString());
                        ProgressDialog.dismiss();

                    } else {
                        ProgressDialog.dismiss();
                        Log.e(ApplicationUtils.ERROR_TAG, task.getException().getMessage());
                    }
                })).addOnFailureListener(e -> {
            ProgressDialog.dismiss();
            Log.e(ApplicationUtils.ERROR_TAG, e.getMessage());
        });
    }

    private void profileUpload(String profileImgUrl) {
        Map<String, Object> map = new HashMap<>();

        map.put("profile_image", profileImgUrl);

        FirebaseRef.getUserRef().child(FirebaseRef.getUserId()).updateChildren(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(mContext, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();

//                        Update user data to shared preference
                        serviceProvider.setProfile_image(profileImgUrl);

                        updateUserToPreference();

                    } else {
                        Log.e(ApplicationUtils.ERROR_TAG, task.getException().getMessage());
                    }
                    ProgressDialog.dismiss();
                });
    }


    private void updateStatus(int status) {
        Map<String, Object> map = new HashMap<>();

        map.put("is_available", status);

        FirebaseRef.getUserRef().child(FirebaseRef.getUserId()).updateChildren(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        if (status == 1)
                            Toast.makeText(mContext, mContext.getString(R.string.status_online_mode), Toast.LENGTH_SHORT).show();
                        else if (status == 0)
                            Toast.makeText(mContext, mContext.getString(R.string.status_offline_mode), Toast.LENGTH_SHORT).show();

                        // Update service provider
                        serviceProvider.setIs_available(status);
                        updateUserToPreference();

                    } else {
                        binding.availability.setChecked(currentAvailability);
                        Toast.makeText(mContext, "Operation failed. Please try again later", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserToPreference() {


        Gson gson = new Gson();
        String json = gson.toJson(serviceProvider);

        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        prefManager.storeSharedData(mContext.getString(R.string.intent_user), json);
    }

    private void openEditProfile() {
        startActivity(new Intent(mContext, ServiceEditProfileActivity.class));
    }


    private void openChangePassDialog() {

        // Re-Authenticate Password
        ChangePassDialog changePassDialog = new ChangePassDialog(this::reAuthenticatePassword);

        changePassDialog.show(mContext.getSupportFragmentManager(), "Change Pass Dialog");
    }

    private void reAuthenticatePassword(String oldPass, String newPass) {
        AuthCredential authCredential = EmailAuthProvider.getCredential(FirebaseRef.getUserEmail(), oldPass);

        FirebaseRef.getCurrentUser().reauthenticate(authCredential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseRef.getCurrentUser().updatePassword(newPass)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {

                                        passSaveInDatabase(newPass);

                                    } else {

                                        Toast.makeText(getActivity(), "Alert! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    } else {
                        Toast.makeText(getActivity(), "Alert! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void passSaveInDatabase(String pass) {
        Map<String, Object> map = new HashMap<>();

        map.put("password", pass);

        FirebaseRef.getUserRef().child(FirebaseRef.getUserId()).updateChildren(map)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(getActivity(), getString(R.string.pass_updated), Toast.LENGTH_SHORT).show();

                    } else {

                        Toast.makeText(getActivity(), "Alert! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void makeCall() {

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            callPermissionResult.launch(Manifest.permission.CALL_PHONE);

        } else {

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:+923025463717"));
            startActivity(Intent.createChooser(callIntent, "Make call..."));

        }

    }

    private void openAboutActivity() {
        startActivity(new Intent(mContext, AboutUsActivity.class));
    }

}
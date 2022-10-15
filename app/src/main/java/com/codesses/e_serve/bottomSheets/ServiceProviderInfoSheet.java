package com.codesses.e_serve.bottomSheets;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.codesses.e_serve.R;
import com.codesses.e_serve.activity.ChatRoomActivity;
import com.codesses.e_serve.activity.ServicerFeedbackActivity;
import com.codesses.e_serve.adapter.ServicerFeedbackAdapter;
import com.codesses.e_serve.databinding.SheetServiceProviderInfoBinding;
import com.codesses.e_serve.enums.Role;
import com.codesses.e_serve.model.User;
import com.codesses.e_serve.utils.Constants;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class ServiceProviderInfoSheet extends BottomSheetDialogFragment {

    public static final String TAG = "ServiceProviderInfoSheet";

    //    Context
    private FragmentActivity mContext;

    //    Data binding
    private SheetServiceProviderInfoBinding binding;

    // Model class
    private User serviceProvider;

    public ServiceProviderInfoSheet(User model) {
        serviceProvider = model;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.sheet_service_provider_info, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Set data
        setData();

        // Click listeners
        binding.ratings.setOnClickListener(v -> openRatingScreen());
        binding.call.setOnClickListener(v -> makeCall());
        binding.chat.setOnClickListener(v -> chat());

    }

    private void openRatingScreen() {
        Intent intent = new Intent(mContext, ServicerFeedbackActivity.class);
        intent.putExtra(Constants.SERVICER_ID, serviceProvider.getUserId());
        startActivity(intent);
    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    ActivityResultLauncher<String> callPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Log.e("CALL_PERMISSION", "onActivityResult: PERMISSION GRANTED");
                } else {
                    Log.e("CALL_PERMISSION", "onActivityResult: PERMISSION DENIED");
                }
            });

    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/


    private void setData() {

        if (serviceProvider.getRole().equals(Role.MECHANIC.toString().toLowerCase()))
            binding.heading.setText(Role.MECHANIC.toString());
        else if (serviceProvider.getRole().equals(Role.RESCUE.toString().toLowerCase()))
            binding.heading.setText(Role.RESCUE.toString());


        binding.businessName.setText(serviceProvider.getBusiness_name());
        binding.name.setText(serviceProvider.getFull_name());
        binding.address.setText(serviceProvider.getLocation().getAddress());

        // Check availability
        if (serviceProvider.getIs_available() == 0)
            binding.availabilityStatus.setVisibility(View.VISIBLE);

    }


    private void makeCall() {
        if (serviceProvider.getIs_available() == 1) {
            String phoneNo = serviceProvider.getPhone_no();

            if (!TextUtils.isEmpty(phoneNo)) {

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    callPermissionResult.launch(Manifest.permission.CALL_PHONE);

                } else {

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + phoneNo));
                    startActivity(Intent.createChooser(callIntent, "Make call..."));

                }

            } else
                Toast.makeText(mContext, "No Phone no available", Toast.LENGTH_SHORT).show();

        } else Toast.makeText(mContext, "Not available", Toast.LENGTH_SHORT).show();

    }


    private void chat() {
        if (serviceProvider.getIs_available() == 1) {

            Intent chatIntent = new Intent(mContext, ChatRoomActivity.class);
            chatIntent.putExtra(Constants.USER_ID, serviceProvider.getUserId());
            startActivity(chatIntent);
            dismiss();

        } else Toast.makeText(mContext, "Not available", Toast.LENGTH_SHORT).show();
    }
}
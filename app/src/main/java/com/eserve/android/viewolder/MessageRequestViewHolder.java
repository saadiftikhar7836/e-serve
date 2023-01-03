package com.eserve.android.viewolder;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eserve.android.databinding.LayoutMessageRequestsBinding;
import com.eserve.android.model.MessageRequest;
import com.eserve.android.model.User;
import com.eserve.android.serviceProvider.activity.UserRequestActivity;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.FirebaseRef;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class MessageRequestViewHolder extends RecyclerView.ViewHolder {

    private LayoutMessageRequestsBinding binding;

    public MessageRequestViewHolder(@NonNull LayoutMessageRequestsBinding itemView) {
        super(itemView.getRoot());
        binding = itemView;


    }

    public void bind(Context mContext, MessageRequest model) {
        FirebaseRef.getUserRef().child(model.getSent_by())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);

                            assert user != null;
                            binding.fullName.setText(user.getFull_name());
                            binding.address.setText(model.getAddress());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Log.d("User_Fail", "onCancelled: ");
                    }
                });


        // Click listener
        itemView.setOnClickListener(v -> {

            // Save request to preference
            ApplicationUtils.saveRequestToPreference(mContext, model);

            // Start activity
            mContext.startActivity(new Intent(mContext, UserRequestActivity.class));

        });

    }
}

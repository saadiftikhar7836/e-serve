package com.codesses.e_serve.viewolder;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codesses.e_serve.databinding.LayoutMessageRequestsBinding;
import com.codesses.e_serve.databinding.LayoutServicerFeedbackBinding;
import com.codesses.e_serve.model.MessageRequest;
import com.codesses.e_serve.model.Ratings;
import com.codesses.e_serve.model.User;
import com.codesses.e_serve.serviceProvider.activity.UserRequestActivity;
import com.codesses.e_serve.utils.ApplicationUtils;
import com.codesses.e_serve.utils.FirebaseRef;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class ServicerFeedbackViewHolder extends RecyclerView.ViewHolder {

    private LayoutServicerFeedbackBinding binding;

    public ServicerFeedbackViewHolder(@NonNull LayoutServicerFeedbackBinding itemView) {
        super(itemView.getRoot());
        binding = itemView;


    }

    public void bind(Context mContext, Ratings model) {

        binding.message.setText(model.getText());
        binding.ratings.setRating(model.getRating());

    }
}

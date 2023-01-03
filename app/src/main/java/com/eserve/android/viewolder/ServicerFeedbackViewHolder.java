package com.eserve.android.viewolder;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eserve.android.databinding.LayoutServicerFeedbackBinding;
import com.eserve.android.model.Ratings;

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

package com.codesses.e_serve.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.codesses.e_serve.R;
import com.codesses.e_serve.adapter.MessageRequestAdapter;
import com.codesses.e_serve.adapter.ServicerFeedbackAdapter;
import com.codesses.e_serve.databinding.ActivityServicerFeedbackBinding;
import com.codesses.e_serve.model.MessageRequest;
import com.codesses.e_serve.model.Ratings;
import com.codesses.e_serve.utils.Constants;
import com.codesses.e_serve.utils.FirebaseRef;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServicerFeedbackActivity extends AppCompatActivity {

    // Context
    AppCompatActivity mContext;

    // Data binding
    ActivityServicerFeedbackBinding binding;

    // ArrayList
    private List<Ratings> ratingsList;


    // Adapter
    private ServicerFeedbackAdapter adapter;


    // Variables
    String servicerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        binding = DataBindingUtil.setContentView(mContext, R.layout.activity_servicer_feedback);

        // Set toolbar
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // ArrayList
        ratingsList = new ArrayList<>();

        // Get intent
        servicerId = getIntent().getStringExtra(Constants.SERVICER_ID);

        // Recycler adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ServicerFeedbackAdapter(mContext, ratingsList);
        binding.recyclerView.setAdapter(adapter);


        // Get ratings
        getRatings();
    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);

    }


    private void getRatings() {
        FirebaseRef.getRatingRef().child(servicerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Ratings ratings = dataSnapshot.getValue(Ratings.class);

                                assert ratings != null;
                                ratingsList.add(ratings);

                            }

                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }
}
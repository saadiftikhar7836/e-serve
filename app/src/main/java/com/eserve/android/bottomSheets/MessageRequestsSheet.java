package com.eserve.android.bottomSheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eserve.android.R;
import com.eserve.android.adapter.MessageRequestAdapter;
import com.eserve.android.databinding.SheetMessageRequestsBinding;
import com.eserve.android.model.MessageRequest;
import com.eserve.android.model.User;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.FirebaseRef;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class MessageRequestsSheet extends DialogFragment {

    public static final String TAG = "MessageRequestsSheet";

    //    Context
    private FragmentActivity mContext;

    //    Data binding
    private SheetMessageRequestsBinding binding;

    // Model class
    private User serviceProvider;


    // ArrayList
    private List<MessageRequest> messageRequestList;


    // Adapter
    private MessageRequestAdapter adapter;


    public MessageRequestsSheet() {
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
        binding = DataBindingUtil.inflate(inflater, R.layout.sheet_message_requests, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize
        messageRequestList = new ArrayList<>();


        // Get service provider data
        serviceProvider = ApplicationUtils.getUserDetail(mContext);


        // Recycler adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MessageRequestAdapter(mContext, messageRequestList);
        binding.recyclerView.setAdapter(adapter);

        // Get message requests
        getMessageRequests();

    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/


    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/


    private void getMessageRequests() {
        Query query = FirebaseRef.getRequestsRef().child(serviceProvider.getUserId()).orderByChild("status").equalTo(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                MessageRequest messageRequest = snapshot.getValue(MessageRequest.class);

                                assert messageRequest != null;
                                messageRequestList.add(messageRequest);

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
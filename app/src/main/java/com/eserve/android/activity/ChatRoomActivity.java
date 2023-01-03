package com.eserve.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.eserve.android.R;
import com.eserve.android.adapter.MessageAdapter;
import com.eserve.android.databinding.ActivityChatRoomBinding;
import com.eserve.android.dialogs.ProgressDialog;
import com.eserve.android.enums.Role;
import com.eserve.android.enums.SharedPrefKey;
import com.eserve.android.fcm.Singleton.VolleySingleton;
import com.eserve.android.model.Chat;
import com.eserve.android.model.MessageRequest;
import com.eserve.android.model.User;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.CheckEmptyFields;
import com.eserve.android.utils.Constants;
import com.eserve.android.utils.FirebaseRef;
import com.eserve.android.utils.SharedPrefManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class ChatRoomActivity extends AppCompatActivity {

    // Context
    AppCompatActivity mContext;

    // Data binding
    ActivityChatRoomBinding binding;

    // Variables
    String userId;
    String message, fcmToken;

    // Model class
    User currentUser, user;

    // Adapter
    MessageAdapter messageAdapter;

    // Lists
    LinkedList<Chat> messageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        binding = DataBindingUtil.setContentView(mContext, R.layout.activity_chat_room);

        // Set toolbar
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Get intent
        userId = getIntent().getStringExtra(Constants.USER_ID);

        //  Getting current user data
        Gson gson = new Gson();
        currentUser = gson.fromJson(SharedPrefManager.getInstance(mContext).getSharedData(mContext.getString(R.string.intent_user)), User.class);

        //  Getting user data
        getUserData();

        // RecyclerView
        messageList = new LinkedList<>();
        messageAdapter = new MessageAdapter(mContext, messageList);
        binding.recyclerView.setAdapter(messageAdapter);
        setAdapter();


        if (currentUser.getRole().equals(Role.CUSTOMER.toString().toLowerCase())) {
            binding.startNav.setVisibility(View.GONE);
        } else {
            binding.startNav.setVisibility(View.VISIBLE);
            binding.startNav.setOnClickListener(v -> startNavigation());
        }

        // Click listener
        binding.send.setOnClickListener(this::sendMessage);
    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);

    }


    /*********************************************************************************************************************************************************
     *                                                                       Click Listeners
     ********************************************************************************************************************************************************/

    private void sendMessage(View view) {
        if (CheckEmptyFields.isEditText(mContext, binding.message.getText().toString().trim(), binding.message)) {

            ProgressDialog.ShowProgressDialog(mContext, R.string.sending, R.string.please_wait);

            String messageId = FirebaseRef.getMessageRef().push().getKey();
            message = binding.message.getText().toString().trim();
            HashMap<String, Object> message = new HashMap<>();
            message.put("m_id", messageId);
            message.put("sender_id", FirebaseRef.getUserId());
            message.put("receiver_id", userId);
            message.put("type", 0);
            message.put("message", this.message);
            message.put("timestamp", String.valueOf(System.currentTimeMillis()));

            FirebaseRef.getMessageRef()
                    .child(FirebaseRef.getUserId())
                    .child(FirebaseRef.getUserId() + userId)
                    .child(messageId)
                    .updateChildren(message)
                    .addOnCompleteListener(task -> {

                    })
                    .addOnFailureListener(e -> Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show());


            int size = messageList.size();
            FirebaseRef.getMessageRef()
                    .child(userId)
                    .child(userId + FirebaseRef.getUserId())
                    .child(messageId)
                    .updateChildren(message)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (size == 0) {
                                sendServiceRequest();
                            }
                            sendNotification(0);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show());
            binding.message.setText("");
            ProgressDialog.DismissProgressDialog();
        }


    }

    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/

    private void getUserData() {

        FirebaseRef.getUserRef().child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);

                        assert user != null;
                        if (user.getRole().equals(Role.MECHANIC.toString().toLowerCase()) || user.getRole().equals(Role.RESCUE.toString().toLowerCase()))
                            setTitle(user.getBusiness_name());
                        else
                            setTitle(user.getFull_name());

                        fcmToken = user.getFcm_token();

                        Log.d("FCM_USER_TOKEN", "sendNavigationPushNotification: " + fcmToken);


                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //    TODO: Send notification to user and save notification to firebase
    private void sendNotification(int type) {

        String nId = FirebaseRef.getNotificationRef().push().getKey();

        Map<String, Object> map = new HashMap<>();

        map.put("n_id", nId);
        map.put("timestamp", System.currentTimeMillis());
        map.put("type", 0);
        map.put("sent_by", FirebaseRef.getUserId());
        map.put("sent_to", userId);
        map.put("title", currentUser.getFull_name());
        map.put("message", "you have new message");
        map.put("is_read", 0);

        assert nId != null;
        FirebaseRef.getNotificationRef().child(nId).updateChildren(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendPushNotification();
                    } else {
                        Log.e("error_tag", task.getException().getMessage());
                    }
                });

    }

    //    TODO: Send push notification to the user
    private void sendPushNotification() {

        try {
            JSONObject mainObject = new JSONObject();
            JSONObject notificationObject = new JSONObject();
            JSONObject dataObject = new JSONObject();
            mainObject.put("to", "/token/" + fcmToken);

//            Notification body
            notificationObject.put("title", "message");
            notificationObject.put("body", mContext.getString(R.string.new_message));

//            Custom payload
            dataObject.put("c_id", currentUser.getUserId());
            dataObject.put("user_name", currentUser.getFull_name());
            mainObject.put("notification", notificationObject);
            mainObject.put("data", dataObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    ApplicationUtils.FCM_URL,
                    mainObject,
                    response -> {

                        Log.d("FCM_RESPONSE", "sendPushNotification: " + response);
                        Toast.makeText(mContext, "Message sent", Toast.LENGTH_SHORT).show();

                    },
                    error -> Log.d("FCM_RESPONSE", "Error " + error)
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("content-type", "application/json");
                    headers.put("authorization", "key=AAAAGTRPKic:APA91bEva9jtdftSFaIvNtROOnGY6Lc65xilvOCje_0XdQGotAZcNWZbcEmXPS8EijfErAQTjXXDHUuEm2pSDezW4X1OnLL7h3bNkc83nd1-fqCjeRb0Dh4G6GOQBM0m19waFFK29Dyl");
                    return headers;
                }
            };

            VolleySingleton.getInstance(mContext).addToRequestQueue(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    //    TODO: Calling method for recyclerView's adapter
    private void setAdapter() {
        FirebaseRef.getMessageRef().child(FirebaseRef.getUserId()).child(FirebaseRef.getUserId() + userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists())
                                messageList.add(dataSnapshot.getValue(Chat.class));
                        }

                        messageAdapter.notifyDataSetChanged();
                        binding.recyclerView.scrollToPosition(messageList.size() - 1);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void sendServiceRequest() {

        String requestId = FirebaseRef.getRequestsRef().push().getKey();
        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);

        Double lat = Double.valueOf(prefManager.getSharedData(SharedPrefKey.LAT.toString()));
        Double lng = Double.valueOf(prefManager.getSharedData(SharedPrefKey.LNG.toString()));

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("r_id", requestId);
        hashMap.put("sent_by", FirebaseRef.getUserId());
        hashMap.put("sent_to", userId);
        hashMap.put("address", prefManager.getSharedData(SharedPrefKey.ADDRESS.toString()));
        hashMap.put("lat", lat);
        hashMap.put("lng", lng);
        hashMap.put("status", 1);


        assert requestId != null;
        FirebaseRef.getRequestsRef().child(userId).child(requestId).updateChildren(hashMap);

    }

    private void startNavigation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setCancelable(false)
                .setMessage(mContext.getString(R.string.text_navigation_surety))
                .setPositiveButton(mContext.getString(R.string.label_navigation), (dialog, which) ->

                        saveNavigationData()

                ).setNegativeButton(mContext.getString(R.string.label_cancel), (dialog, which) -> dialog.dismiss());

        builder.create().show();

    }

    private void saveNavigationData() {

        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);

        HashMap<String, Object> navigationMap = new HashMap<>();
        MessageRequest messageRequest;
        messageRequest = ApplicationUtils.getUserRequest(mContext);


        navigationMap.put("request_id", messageRequest.getR_id());
        navigationMap.put("user_lat", messageRequest.getLat());
        navigationMap.put("user_lng", messageRequest.getLng());
        navigationMap.put("service_lat", Double.parseDouble(prefManager.getSharedData(SharedPrefKey.LAT.toString())));
        navigationMap.put("service_lng", Double.parseDouble(prefManager.getSharedData(SharedPrefKey.LNG.toString())));
        navigationMap.put("sent_by", currentUser.getUserId());
        navigationMap.put("sent_to", userId);
        navigationMap.put("service_role", currentUser.getRole());
        navigationMap.put("servicer_name", currentUser.getFull_name());
        navigationMap.put("customer_name", user.getFull_name());
        navigationMap.put("nav_id", currentUser.getUserId() + userId);
        navigationMap.put("customer_fcm_token", fcmToken);


        FirebaseRef.getNavigationRef().child(currentUser.getUserId())
                .child(currentUser.getUserId() + userId)
                .updateChildren(navigationMap);

        // Set servicer offline during navigation
        FirebaseRef.getUserRef().child(currentUser.getUserId()).child("is_available").setValue(0);

        navigationMap.put("nav_id", userId + currentUser.getUserId());

        FirebaseRef.getNavigationRef().child(userId)
                .child(userId + currentUser.getUserId())
                .updateChildren(navigationMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendNavigationNotification();
                        Intent intent = new Intent(mContext, RoutingActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });


    }

    private void sendNavigationNotification() {
        String nId = FirebaseRef.getNotificationRef().push().getKey();

        Map<String, Object> map = new HashMap<>();

        map.put("n_id", nId);
        map.put("timestamp", System.currentTimeMillis());
        map.put("type", 1);
        map.put("sent_by", FirebaseRef.getUserId());
        map.put("sent_to", userId);
        map.put("title", currentUser.getFull_name());
        map.put("message", "navigation started");
        map.put("is_read", 0);

        assert nId != null;
        FirebaseRef.getNotificationRef().child(nId).updateChildren(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendNavigationPushNotification();
                    } else {
                        Log.e("error_tag", task.getException().getMessage());
                    }
                });
    }

    private void sendNavigationPushNotification() {

        try {
            JSONObject mainObject = new JSONObject();
            JSONObject notificationObject = new JSONObject();
            JSONObject dataObject = new JSONObject();
            mainObject.put("to", "/token/" + fcmToken);

//            Notification body
            notificationObject.put("title", "message");
            notificationObject.put("body", mContext.getString(R.string.new_message));

//            Custom payload
            dataObject.put("n_id", currentUser.getUserId());
            dataObject.put("user_name", currentUser.getFull_name());
            mainObject.put("notification", notificationObject);
            mainObject.put("data", dataObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    ApplicationUtils.FCM_URL,
                    mainObject,
                    response -> {

                        Log.d("FCM_RESPONSE", "sendPushNotification: " + response);
                        Toast.makeText(mContext, "Message sent", Toast.LENGTH_SHORT).show();

                    },
                    error -> Log.d("FCM_RESPONSE", "Error " + error)
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("content-type", "application/json");
                    headers.put("authorization", "key=AAAAGTRPKic:APA91bEva9jtdftSFaIvNtROOnGY6Lc65xilvOCje_0XdQGotAZcNWZbcEmXPS8EijfErAQTjXXDHUuEm2pSDezW4X1OnLL7h3bNkc83nd1-fqCjeRb0Dh4G6GOQBM0m19waFFK29Dyl");
                    return headers;
                }
            };

            VolleySingleton.getInstance(mContext).addToRequestQueue(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
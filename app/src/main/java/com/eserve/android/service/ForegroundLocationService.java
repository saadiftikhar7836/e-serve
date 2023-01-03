package com.eserve.android.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.eserve.android.R;
import com.eserve.android.activity.RoutingActivity;
import com.eserve.android.enums.SharedPrefKey;
import com.eserve.android.fcm.Singleton.VolleySingleton;
import com.eserve.android.model.Navigation;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.Constants;
import com.eserve.android.utils.FirebaseRef;
import com.eserve.android.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForegroundLocationService extends Service implements LocationListener {

    private static final String TAG = "MyLocationService";
    private static final String CHANNEL_DEFAULT_IMPORTANCE = "my service notification";
    private static final String REACH_NOTIFICATION_CHANNEL = "reached notification channel";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 0;
    private static final float LOCATION_DISTANCE = 0f;
    NotificationManager notificationManager;
    Notification notification;
    Context mContext;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        if (intent.getAction() != null && intent.getAction().equals("stop_action")) {
            stopForeground(true);
            stopService(new Intent(getApplicationContext(), ForegroundLocationService.class));
            mLocationManager.removeUpdates(this);
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification("");
        }

        initializeLocationManager();

        postLocation();

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.e("Manager", provider);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, this);
            else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 5, this);

        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.e("Manager", provider);
    }

    private void postLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;

        } else {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
            else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);

        }
    }


    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager - LOCATION_INTERVAL: " + LOCATION_INTERVAL + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, location.getLatitude() + " Long:" + location.getLongitude());

        // Get data from shared preference
        String userId = SharedPrefManager.getInstance(mContext).getSharedData(SharedPrefKey.NAVIGATION_USER_ID.toString());


        // Get destination latLng and address
        double customerLat = Double.parseDouble(SharedPrefManager.getInstance(mContext).getSharedData(SharedPrefKey.USER_LAT.toString()));
        double customerLng = Double.parseDouble(SharedPrefManager.getInstance(mContext).getSharedData(SharedPrefKey.USER_LNG.toString()));


        // Get current navigation
        Navigation navigation = ApplicationUtils.getNavigation(mContext);


        // Get destination address
        String destinationAddress = ApplicationUtils.getAddress(mContext, customerLat, customerLng);


        // Navigation map
        HashMap<String, Object> navigationMap = new HashMap<>();

        navigationMap.put("service_lat", location.getLatitude());
        navigationMap.put("service_lng", location.getLongitude());


        // Update data in current navigation both customer and servicer
        FirebaseRef.getNavigationRef().child(FirebaseRef.getUserId())
                .child(FirebaseRef.getUserId() + userId)
                .updateChildren(navigationMap);

        FirebaseRef.getNavigationRef().child(userId)
                .child(userId + FirebaseRef.getUserId())
                .updateChildren(navigationMap);

        // Check near to destination
        if (navigation != null && isNearToReach(navigation, location)) {

            // Update shared preference
            SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
            prefManager.storeSharedData(Constants.IS_NEAR_TO_REACH, true);

            // Generate reached notification
            reachedNotification();

            // Notify customer
            sendNotificationToCustomer(navigation.getCustomer_fcm_token());
        }


        // Create notification
        createNotification(destinationAddress);

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    void createNotification(String address) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_DEFAULT_IMPORTANCE, "notification_name", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);

        Intent stopServiceIntent = new Intent(getApplicationContext(), ForegroundLocationService.class);
        Intent startMainActivity = new Intent(getApplicationContext(), RoutingActivity.class);

        startMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        stopServiceIntent.setAction("stop_action");

        PendingIntent startActivity = PendingIntent.getActivity(getApplicationContext(), 1, startMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                .setContentTitle("Destination")
                .setContentText(address)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setColor(ContextCompat.getColor(mContext, R.color.colorGreen))
                .setColorized(true)
                .setOngoing(true)
                .setSmallIcon(R.drawable.app_logo)
                .setAutoCancel(false)
                .setContentIntent(startActivity)
                .build();


        // Notification ID cannot be 0.
        startForeground(1, notification);

    }


    private boolean isNearToReach(Navigation navigation, Location location) {

        float[] distance = new float[2];

        double servicerLat = location.getLatitude();
        double servicerLng = location.getLongitude();
        double customerLat = navigation.getUser_lat();
        double customerLng = navigation.getUser_lng();

        Location.distanceBetween(servicerLat, servicerLng,
                customerLat, customerLng, distance);

        return distance[0] <= ApplicationUtils.CIRCLE_RADIUS_40m;

    }

    private void reachedNotification() {

        Intent intent = new Intent(mContext, RoutingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(REACH_NOTIFICATION_CHANNEL, "reached_notification", NotificationManager.IMPORTANCE_HIGH);


            Notification notification = new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                    .setContentTitle("Reached")
                    .setContentText("You are about to reach just 40m away from your destination")
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.app_logo)
                    .setContentIntent(pendingIntent)
                    .build();

            notificationManager.createNotificationChannel(channel);

            notificationManager.notify(1, notification);
        }
    }

    private void sendNotificationToCustomer(String fcmToken) {

        try {
            JSONObject mainObject = new JSONObject();
            JSONObject notificationObject = new JSONObject();
            mainObject.put("to", "/token/" + fcmToken);

//            Notification body
            notificationObject.put("title", "Reached");
            notificationObject.put("body", "Your service provider has been reached");


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

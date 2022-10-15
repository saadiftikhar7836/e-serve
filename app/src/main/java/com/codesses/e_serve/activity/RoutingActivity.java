package com.codesses.e_serve.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.codesses.e_serve.R;
import com.codesses.e_serve.adapter.MapMarkerInfoAdapter;
import com.codesses.e_serve.bottomSheets.RateExperienceSheet;
import com.codesses.e_serve.databinding.ActivityRoutingBinding;
import com.codesses.e_serve.enums.Role;
import com.codesses.e_serve.enums.SharedPrefKey;
import com.codesses.e_serve.model.Navigation;
import com.codesses.e_serve.model.User;
import com.codesses.e_serve.service.ForegroundLocationService;
import com.codesses.e_serve.service.LocationService;
import com.codesses.e_serve.serviceProvider.activity.ServiceMainActivity;
import com.codesses.e_serve.utils.ApplicationUtils;
import com.codesses.e_serve.utils.Constants;
import com.codesses.e_serve.utils.FirebaseRef;
import com.codesses.e_serve.utils.SharedPrefManager;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoutingActivity extends AppCompatActivity implements OnMapReadyCallback {


    //    binding object
    ActivityRoutingBinding binding;

    //    context object
    AppCompatActivity mContext;

    //   google map object
    GoogleMap mMap;

    //    location settings response task object
    Task<LocationSettingsResponse> task;

    // Model class
    User user;

    // Variables
    boolean isCameraUpdated = false;
    boolean isRatingSheetOpened = false;

    //    marker object
    Marker customerMarker, serviceMarker;


    private LatLng destination, origin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        binding = DataBindingUtil.setContentView(mContext, R.layout.activity_routing);


        // Get current user details
        user = ApplicationUtils.getUserDetail(mContext);

        initialization();

        // Update navigation running
        if (!isNavTrue()) {
            SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
            prefManager.storeSharedData(Constants.IS_NAVIGATION_RUNNING, true);
        }

        if (isRatingSheet()) {
            // Open rate experience bottomSheet
            openRatingSheet();
        }
    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        // Set map settings and styles
        setStylesAndSettings(mMap);

        // Get current location
        if (!user.getRole().equals(Role.CUSTOMER.toString().toLowerCase())) {
            if (isLocationPermission()) {
                preparedLocationRequest();
            } else {
                requestLocationPermission();
            }
        }
        getNavigationData();

    }

    ActivityResultLauncher<String> locationPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {

                if (result) {
                    Log.e("LOCATION_PERMISSION", "onActivityResult: PERMISSION GRANTED");
                    preparedLocationRequest();

                } else {
                    Log.e("LOCATION_PERMISSION", "onActivityResult: PERMISSION DENIED");
                }

            });

    ActivityResultLauncher<IntentSenderRequest> resolutionForResult =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), activityResult ->
            {
                if (activityResult.getResultCode() == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        ContextCompat.startForegroundService(mContext, new Intent(mContext, ForegroundLocationService.class));
                    else
                        startService(new Intent(mContext, LocationService.class));
                }
                //startLocationUpdates() or do whatever you want
                else {
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            if (e instanceof ResolvableApiException) {
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(resolvable.getResolution().getIntentSender()).build();
                                resolutionForResult.launch(intentSenderRequest);
                            }
                        }
                    });
                }
            });


    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/


    //    function for initialization
    private void initialization() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map_fragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    private void setStylesAndSettings(GoogleMap mMap) {

        // Set map settings
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Set custom style
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            mContext, R.raw.map_style));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

    }

    private boolean isNavTrue() {

        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        return prefManager.getBooleanData(Constants.IS_NAVIGATION_RUNNING);

    }

    public void preparedLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(mContext);
        task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(mContext, locationSettingsResponse -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(new Intent(mContext, ForegroundLocationService.class));
            else
                startService(new Intent(mContext, LocationService.class));

        });

        task.addOnFailureListener(mContext, e -> {

            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(resolvable.getResolution().getIntentSender()).build();
                resolutionForResult.launch(intentSenderRequest);
            }

        });
    }

    private boolean isLocationPermission() {
        return ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

    }

    private void getNavigationData() {

        FirebaseRef.getNavigationRef().child(user.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                mMap.clear();
                                Navigation navigation = dataSnapshot.getValue(Navigation.class);

                                assert navigation != null;
                                origin = new LatLng(navigation.getService_lat(), navigation.getService_lng());
                                destination = new LatLng(navigation.getUser_lat(), navigation.getUser_lng());

                                if (!user.getRole().equals(Role.CUSTOMER.toString().toLowerCase())) {

                                    SharedPrefManager.getInstance(mContext).storeSharedData(SharedPrefKey.NAVIGATION_USER_ID, navigation.getSent_to());
                                    SharedPrefManager.getInstance(mContext).storeSharedData(SharedPrefKey.USER_LAT, navigation.getUser_lat());
                                    SharedPrefManager.getInstance(mContext).storeSharedData(SharedPrefKey.USER_LNG, navigation.getUser_lng());

                                }

                                // Save navigation to shared preference
                                ApplicationUtils.saveNavigationToPreference(mContext, navigation);

                                if (isNearToReach(navigation)) {
                                    // servicer near to customer
                                    nearToReachManage(navigation);
                                }

                                setServiceMarker(navigation);
                                setCustomerMarker(navigation);
                                showRouting();

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    /**
     * Draw polyline on map, get distance and duration of the route
     * <p>
     * latLngDestination LatLng of the destination
     */
    private void showRouting() {

        // Using AK Exorcist Google Direction Library
        GoogleDirection.withServerKey(mContext.getString(R.string.google_maps_api_key))
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        String status = direction.getStatus();
                        if (status.equals(RequestResult.OK)) {
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);

                            // Get distance and duration
                            String distance = leg.getDistance().getText();
                            String duration = leg.getDuration().getText();

                            // Show distance
                            showTimeDistance(distance, duration);

                            // Drawing path
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(mContext,
                                    directionPositionList, 8, getResources().getColor(R.color.colorGreen));
                            mMap.addPolyline(polylineOptions);

                            // Zooming the map according to marker bounds
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(origin);
                            builder.include(destination);
                            LatLngBounds bounds = builder.build();

                            int width = getResources().getDisplayMetrics().widthPixels;
                            int height = getResources().getDisplayMetrics().heightPixels;
                            int padding = (int) (width * 0.1); // offset from edges of the map 10% of screen

                            // Update camera
                            if (!isCameraUpdated) {
                                isCameraUpdated = true; // It will be update once until the activity refresh
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                                mMap.animateCamera(cameraUpdate);
                            }

                        } else if (status.equals(RequestResult.NOT_FOUND)) {
                            Toast.makeText(mContext, "No routes exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something here

                    }
                });

    }


    private void setCustomerMarker(Navigation navigation) {

        MarkerOptions markerOptions = new MarkerOptions()
                .position(destination)
                .title(navigation.getCustomer_name())
                .snippet(ApplicationUtils.getAddress(mContext, navigation.getUser_lat(), navigation.getUser_lng()))
                .icon(bitmapDescriptorFromVector(R.drawable.ic_marker));

        if (customerMarker != null)
            customerMarker.remove();

        customerMarker = mMap.addMarker(markerOptions);

    }

    private void setServiceMarker(Navigation navigation) {

        int markerIcon = 0;

        if (navigation.getService_role().equals(Role.MECHANIC.toString().toLowerCase()))
            markerIcon = R.drawable.ic_marker_mechanic;
        else if (navigation.getService_role().equals(Role.RESCUE.toString().toLowerCase()))
            markerIcon = R.drawable.ic_marker_rescue;


        MarkerOptions markerOptions = new MarkerOptions()
                .position(origin)
                .title(navigation.getServicer_name())
                .snippet(ApplicationUtils.getAddress(mContext, navigation.getService_lat(), navigation.getService_lng()))
                .icon(bitmapDescriptorFromVector(markerIcon));

        if (serviceMarker != null)
            serviceMarker.remove();
        serviceMarker = mMap.addMarker(markerOptions);

        //        Set custom marker title
        mMap.setInfoWindowAdapter(new MapMarkerInfoAdapter(mContext));


    }

    private void showTimeDistance(String distance, String duration) {

        String message = "Total Distance is " + distance + " and Estimated Time is " + duration;
        Log.d("DISTANCE_DURATION", "onDirectionSuccess: " + message);

        binding.distance.setText(distance);
        binding.duration.setText(duration);

    }

    private BitmapDescriptor bitmapDescriptorFromVector(@DrawableRes int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(mContext, vectorResId);

        assert vectorDrawable != null;
        vectorDrawable.setBounds(
                0,
                0,
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight()
        );
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private boolean isNearToReach(Navigation navigation) {

        float[] distance = new float[2];

        double servicerLat = navigation.getService_lat();
        double servicerLng = navigation.getService_lng();
        double customerLat = navigation.getUser_lat();
        double customerLng = navigation.getUser_lng();

        Location.distanceBetween(servicerLat, servicerLng,
                customerLat, customerLng, distance);

        return distance[0] <= ApplicationUtils.CIRCLE_RADIUS_40m;

    }

    private boolean isRatingSheet() {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        return prefManager.getBooleanData(Constants.IS_RATE_EXPERIENCE_READY);
    }

    private void nearToReachManage(Navigation navigation) {

        if (user.getRole().equals(Role.CUSTOMER.toString().toLowerCase())) {

            // Update shared preference
            SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
            prefManager.storeSharedData(Constants.IS_RATE_EXPERIENCE_READY, true);


            // Open rate experience bottomSheet
            openRatingSheet();

        } else if (isServicerNearToReach()) {
            createStopServiceDialog();
        }
    }


    private void openRatingSheet() {
        if (!isRatingSheetOpened) {
            isRatingSheetOpened = true;
            RateExperienceSheet sheet = new RateExperienceSheet();
            sheet.show(mContext.getSupportFragmentManager(), RateExperienceSheet.TAG);
        }
    }

    private boolean isServicerNearToReach() {
        if (user.getRole().equals(Role.MECHANIC.toString().toLowerCase())
                || user.getRole().equals(Role.RESCUE.toString().toLowerCase())) {

            SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
            return prefManager.getBooleanData(Constants.IS_NEAR_TO_REACH);

        } else return false;
    }


    private void createStopServiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);

        builder.setTitle("Reached")
                .setMessage("You've reached your destination. Press ok to exit navigation")
                .setPositiveButton("Ok", (dialog, which) -> {
                    Intent intent;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        intent = new Intent(mContext, ForegroundLocationService.class);
                    else
                        intent = new Intent(mContext, LocationService.class);

                    intent.setAction("stop_action");
                    stopService(intent);

                    // Remove servicer navigation
                    removeServicerNavigation();


                }).create().show();
    }

    private void removeServicerNavigation() {

        Map<String, Object> map = new HashMap<>();

        // Delete navigation
        map.put("navigation/" + user.getUserId(), null);


        FirebaseRef.getDatabaseInstance()
                .updateChildren(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Update shared preference
                        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
                        prefManager.storeSharedData(Constants.IS_NAVIGATION_RUNNING, false);
                        prefManager.clearString(Constants.CURRENT_NAVIGATION);
                        prefManager.clearString(Constants.MESSAGE_REQUESTS);

                        // Set servicer available again after providing service
                        FirebaseRef.getUserRef().child(user.getUserId()).child("is_available").setValue(1);

                        // Start Service provider main activity
                        startActivity(new Intent(mContext, ServiceMainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(mContext, "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                    }

                });
    }

}
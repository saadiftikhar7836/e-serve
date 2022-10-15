package com.codesses.e_serve.customer.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.codesses.e_serve.R;
import com.codesses.e_serve.adapter.MapMarkerInfoAdapter;
import com.codesses.e_serve.bottomSheets.ServiceProviderInfoSheet;
import com.codesses.e_serve.databinding.FragmentCustomerHomeBinding;
import com.codesses.e_serve.enums.Role;
import com.codesses.e_serve.enums.SharedPrefKey;
import com.codesses.e_serve.model.ServiceLocation;
import com.codesses.e_serve.model.User;
import com.codesses.e_serve.utils.ApplicationUtils;
import com.codesses.e_serve.utils.FirebaseRef;
import com.codesses.e_serve.utils.ProgressDialog;
import com.codesses.e_serve.utils.SharedPrefManager;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class CustomerHomeFragment extends Fragment {

    // Context
    private FragmentActivity mContext;

    // Data binding
    private FragmentCustomerHomeBinding binding;


    // Google map
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedClient;
    private GoogleMap mMap;
    private Marker marker;
    private Circle mCircle;
    private LatLng mCircleCenter;
    private Map<String, String> mMarkerMap = new HashMap<>();


    Location location;


    // Variables
    private double latitude,
            longitude;
    long totalServicerCount = 0, servicerCount = 0;

    // Model class
    private User user;

    // ArrayList
    private List<User> serviceProviderList;
    private List<LatLng> mPoints = new ArrayList<>();
    private List<Marker> serviceMarkerList;


    // Gps location
    private Task<LocationSettingsResponse> task;
    private LocationCallback locationCallback;


    List<Address> address;

    public CustomerHomeFragment() {
        // Required empty public constructor
    }


    public static CustomerHomeFragment newInstance(String param1, String param2) {
        CustomerHomeFragment fragment = new CustomerHomeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_home, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize
        initialization();


        // Get current user details
        user = ApplicationUtils.getUserDetail(mContext);

        // Save map state
        binding.mapView.onCreate(savedInstanceState);

        // Create map
        binding.mapView.getMapAsync(googleMap -> {
            mMap = googleMap;

            // Set map settings and styles
            setStylesAndSettings(mMap);

            // Get current location
            preparedLocationRequest();
//            getCurrentLocation();

            // Marker click listener
            markerClickListener();

        });


        // Click listener
        binding.chooseMechanic.setOnClickListener(v -> getServiceProviders(Role.MECHANIC.toString().toLowerCase()));
        binding.chooseRescue.setOnClickListener(v -> getServiceProviders(Role.RESCUE.toString().toLowerCase()));
        binding.currentLocation.setOnClickListener(v -> getCurrentLocation());

    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    @Override
    public void onResume() {
        binding.mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }


    private final ActivityResultLauncher<String> locationPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {

                if (result) {
                    Log.e("LOCATION_PERMISSION", "onActivityResult: PERMISSION GRANTED");
                    preparedLocationRequest();

                } else {
                    Log.e("LOCATION_PERMISSION", "onActivityResult: PERMISSION DENIED");
                }

            });


    private final ActivityResultLauncher<Intent> enableGpsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                if (result.getResultCode() == RESULT_OK)
                    preparedLocationRequest();
                else
                    Toast.makeText(mContext, mContext.getString(R.string.text_gps_not_enable), Toast.LENGTH_SHORT).show();

            });

    private final ActivityResultLauncher<IntentSenderRequest> resolutionForResult =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), activityResult ->
            {
                if (activityResult.getResultCode() == RESULT_OK) {
                    preparedLocationRequest();
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

    private void initialization() {

        // Location manager
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        fusedClient = LocationServices.getFusedLocationProviderClient(mContext);

        // ArrayList
        serviceProviderList = new ArrayList<>();
        serviceMarkerList = new ArrayList<>();

    }

    public void preparedLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(mContext);
        task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(mContext, locationSettingsResponse -> getCurrentLocation());

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

    private boolean isGpsEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void onGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage(mContext.getString(R.string.text_enable_gps))
                .setCancelable(false)
                .setPositiveButton(mContext.getString(R.string.label_yes), (dialog, which) -> {
                    enableGpsLauncher.launch(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                }).setNegativeButton(mContext.getString(R.string.label_no), (dialog, which) -> {

        });

        builder.create().show();

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

    public void getCurrentLocation() {
        if (isLocationPermission()) {
            fusedClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        location = task.getResult();
                        if (location != null) {
                            cameraUpdate(location);
                        } else {

                            final LocationRequest locationRequest = LocationRequest.create();
                            locationRequest.setInterval(10000);
                            locationRequest.setFastestInterval(5000);
                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            locationCallback = new LocationCallback() {
                                @Override
                                public void onLocationResult(@NotNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    location = locationResult.getLastLocation();
                                    cameraUpdate(location);
                                    fusedClient.removeLocationUpdates(locationCallback);

                                }
                            };

                            fusedClient.requestLocationUpdates(locationRequest, locationCallback, null);

                        }
                    }
                }
            });
        } else requestLocationPermission();
    }

    private void cameraUpdate(@NotNull Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        mCircleCenter = latLng;

        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        prefManager.storeSharedData(SharedPrefKey.LAT, latitude);
        prefManager.storeSharedData(SharedPrefKey.LNG, longitude);

        Geocoder geocoder = new Geocoder(mContext);

        try {
            address = geocoder.getFromLocation(latitude, longitude, 5);
            if (address != null && address.size() > 0)
                prefManager.storeSharedData(SharedPrefKey.ADDRESS, address.get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Add marker
        markerOptions.position(latLng)
                .title(user.getFull_name())
                .icon(bitmapDescriptorFromVector(R.drawable.ic_marker));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13F));

        if (mMap != null && marker == null)
            marker = mMap.addMarker(markerOptions);

        // Draw circle
        assert marker != null;
        drawCircle();

//        Set custom marker title
        mMap.setInfoWindowAdapter(new MapMarkerInfoAdapter(mContext));


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


    /**************
     * Draw circle
     */

    protected void drawCircle() {

        if (mCircle == null) {

            CircleOptions circleOptions = new CircleOptions()
                    .center(marker.getPosition())
                    .radius(ApplicationUtils.CIRCLE_RADIUS_3km)
                    .strokeWidth(0.5F)
                    .strokeColor(ContextCompat.getColor(mContext, R.color.navy))
                    .fillColor(0x55b3b8c3);

            mCircle = mMap.addCircle(circleOptions);

        }

    }

    /*****************
     * Choose service
     */
    private void getServiceProviders(String service) {

        // Reset service provider count
        totalServicerCount = 0;
        servicerCount = 0;


        // Show dialog
        ProgressDialog.show(mContext, 0, R.string.data_being_fetched);

        // Handle service states
        handleServiceStates(service);


        // Remove markers
        if (serviceMarkerList.size() != 0)
            removeAllMarkers();

        // Get info
        Query roleQuery = FirebaseRef.getUserRef().orderByChild("role").equalTo(service);

        roleQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    totalServicerCount = dataSnapshot.getChildrenCount(); // no service provider run at the end of the loop

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        servicerCount++; // no service provider run at the end of the loop
                        User serviceProvider = snapshot.getValue(User.class);

                        assert serviceProvider != null;
                        serviceProvider.setUserId(snapshot.getRef().getKey());
                        ServiceLocation location = snapshot.child("location").getValue(ServiceLocation.class);
                        serviceProvider.setLocation(location);


                        if (isWithin3km(serviceProvider)) {

                            // Add service provider to list
                            serviceProviderList.add(serviceProvider);


                            // Add service provider markers
                            addServiceMarkers(serviceProvider);

                            Log.d("SERVICE_PROVIDER", "onDataChange: " + serviceProviderList.size() +
                                    " || " +
                                    servicerCount
                                    + serviceProvider.getRole());

                        } else {
                            Log.d("No_SERVICE", "onDataChange: circle else");
                            noServiceWithIn3km();
                        }

                    }
                } else {
                    Log.d("No_SERVICE", "onDataChange: main else");

                    noServiceWithIn3km();
                }

            }


            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.e("SERVICE_PROVIDER_FAIL", error.getMessage());
            }
        });

        // Dismiss dialog
        ProgressDialog.dismiss();

    }

    private void handleServiceStates(String service) {

        if (service.equals(Role.MECHANIC.toString().toLowerCase())) {

            binding.chooseMechanic.setEnabled(false);
            binding.chooseRescue.setEnabled(true);

            binding.chooseMechanic.setBackgroundResource(R.drawable.circle_green);
            binding.chooseMechanic.setColorFilter(ContextCompat.getColor(mContext, R.color.white));

            binding.chooseRescue.setBackgroundResource(R.drawable.circle_white);
            binding.chooseRescue.setColorFilter(ContextCompat.getColor(mContext, R.color.navy));

        } else if (service.equals(Role.RESCUE.toString().toLowerCase())) {

            binding.chooseRescue.setEnabled(false);
            binding.chooseMechanic.setEnabled(true);

            binding.chooseRescue.setBackgroundResource(R.drawable.circle_green);
            binding.chooseRescue.setColorFilter(ContextCompat.getColor(mContext, R.color.white));

            binding.chooseMechanic.setBackgroundResource(R.drawable.circle_white);
            binding.chooseMechanic.setColorFilter(ContextCompat.getColor(mContext, R.color.navy));

        }

    }

    private boolean isWithin3km(User serviceProvider) {

        mCircle.setRadius(ApplicationUtils.CIRCLE_RADIUS_3km);
        float[] distance = new float[2];

        double lat = serviceProvider.getLocation().getLat();
        double lon = serviceProvider.getLocation().getLng();

        Location.distanceBetween(lat, lon, mCircleCenter.latitude,
                mCircleCenter.longitude, distance);

        return distance[0] <= ApplicationUtils.CIRCLE_RADIUS_3km;

    }

    private void addServiceMarkers(User model) {

        LatLng latLng = new LatLng(model.getLocation().getLat(),
                model.getLocation().getLng());

        if (mMap != null) {

            int markerIcon = 0;

            // Change icon on the basis of service provider role
            if (model.getRole().equals(Role.MECHANIC.toString().toLowerCase()))
                markerIcon = R.drawable.ic_marker_mechanic;
            else if (model.getRole().equals(Role.RESCUE.toString().toLowerCase()))
                markerIcon = R.drawable.ic_marker_rescue;


            // Add marker
            Marker serviceMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(bitmapDescriptorFromVector(markerIcon))
                    .title(model.getBusiness_name())
                    .snippet(model.getLocation().getAddress())
            );

            serviceMarkerList.add(serviceMarker); // Add markers in the list

//            assert marker != null;
//            mMarkerMap.put(marker.getId(), model.getLocation().getPlace_id());

        }
    }

    private void noServiceWithIn3km() {

        if (servicerCount == totalServicerCount && serviceProviderList.size() == 0) {
            Snackbar snackbar = Snackbar.make(binding.root,
                    mContext.getString(R.string.no_service_provider),
                    Snackbar.LENGTH_SHORT);
            snackbar.show();
        }

    }

    private void removeAllMarkers() {
        for (Marker marker : serviceMarkerList) {
            marker.remove();
        }
        serviceMarkerList.clear();
        serviceProviderList.clear();
    }


    /******************
     * Click listeners
     */

    private void markerClickListener() {

        mMap.setOnMarkerClickListener(marker -> {


            double lat = marker.getPosition().latitude;
            double lng = marker.getPosition().longitude;

            for (int i = 0; i < serviceProviderList.size(); i++) {

                User model = serviceProviderList.get(i);

                if (model.getLocation().getLat().equals(lat) &&
                        model.getLocation().getLng().equals(lng)) {

                    // Open bottom sheet
                    ServiceProviderInfoSheet sheet = new ServiceProviderInfoSheet(model);
                    sheet.show(mContext.getSupportFragmentManager(), ServiceProviderInfoSheet.TAG);

                    Log.d("Marker_Position", "markerClickListener: " + lat + " | " + lng + " | " + model.getFull_name());

                }

            }

            return false;
        });
    }


}
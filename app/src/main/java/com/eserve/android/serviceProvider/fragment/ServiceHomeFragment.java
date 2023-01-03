package com.eserve.android.serviceProvider.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
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

import com.eserve.android.R;
import com.eserve.android.adapter.MapMarkerInfoAdapter;
import com.eserve.android.bottomSheets.MessageRequestsSheet;
import com.eserve.android.databinding.FragmentServiceHomeBinding;
import com.eserve.android.enums.Role;
import com.eserve.android.enums.SharedPrefKey;
import com.eserve.android.utils.FirebaseRef;
import com.eserve.android.model.User;
import com.eserve.android.utils.ApplicationUtils;
import com.eserve.android.utils.SharedPrefManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class ServiceHomeFragment extends Fragment {

    // Context
    private FragmentActivity mContext;

    // Data binding
    private FragmentServiceHomeBinding binding;


    // Google map
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedClient;
    private GoogleMap mMap;
    private Marker marker;
    private LatLng origin, destination;


    // Model class
    private User serviceProvider;


    // Variables
    private long requestsCount = 0;


    public ServiceHomeFragment() {
        // Required empty public constructor
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_home, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize
        initialization();


        // Get current user details
        serviceProvider = ApplicationUtils.getUserDetail(mContext);


        // Save map state
        binding.mapView.onCreate(savedInstanceState);


//        // Create map
        binding.mapView.getMapAsync(googleMap -> {
            mMap = googleMap;

            // Set map settings and styles
            setStylesAndSettings(mMap);

            // Get current location
            getCurrentLocation();


        });


        // Get message requests
        getMessageRequestCount();

        // Get total orders
        getTotalOrdersCount();


        // Click listeners
        binding.clMessageRequest.setOnClickListener(v -> openMessageRequests());

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


    ActivityResultLauncher<String> locationPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {

                if (result) {
                    Log.e("LOCATION_PERMISSION", "onActivityResult: PERMISSION GRANTED");
                    if (!isGpsEnabled()) onGps();
                    else getCurrentLocation();

                } else {
                    Log.e("LOCATION_PERMISSION", "onActivityResult: PERMISSION DENIED");
                }

            });


    ActivityResultLauncher<Intent> enableGpsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("GPS_KA_CALL_BACK", ": " + result);
                    ServiceHomeFragment.this.getCurrentLocation();
                }
            });

    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/

    private void initialization() {

        // Location manager
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        fusedClient = LocationServices.getFusedLocationProviderClient(mContext);

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

    private void getCurrentLocation() {

        if (isLocationPermission()) {

            if (!isGpsEnabled()) {
                Log.d("GPS_CALL_BACK", "if: ");

                onGps();

            } else {

                fusedClient.getLastLocation().addOnSuccessListener(location -> {

                    if (location != null) {
                        cameraUpdate(location);
                    } else getCurrentLocation();

                }).addOnFailureListener(e -> getCurrentLocation());

            }

        } else requestLocationPermission();

    }

    private void cameraUpdate(Location location) {

        // Variables
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        origin = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();

//        destination = new LatLng(31.5832628, 74.37996199999999); // for test purpose

        // Change icon on the basis of service provider role
        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        prefManager.storeSharedData(SharedPrefKey.LAT, latitude);
        prefManager.storeSharedData(SharedPrefKey.LNG, longitude);

        int markerIcon = 0;


        if (serviceProvider.getRole().equals(Role.MECHANIC.toString().toLowerCase()))
            markerIcon = R.drawable.ic_marker_mechanic;
        else if (serviceProvider.getRole().equals(Role.RESCUE.toString().toLowerCase()))
            markerIcon = R.drawable.ic_marker_rescue;


        // Add marker
        markerOptions.position(origin)
                .title(serviceProvider.getFull_name())
                .icon(bitmapDescriptorFromVector(markerIcon));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13F));

        if (mMap != null && marker == null)
            marker = mMap.addMarker(markerOptions);

        // Set custom marker title
        assert mMap != null;
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


    private void getMessageRequestCount() {
        Query query = FirebaseRef.getRequestsRef().child(serviceProvider.getUserId()).orderByChild("status").equalTo(1);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren())
                    requestsCount = dataSnapshot.getChildrenCount();

                binding.countMessageRequest.setText(String.valueOf(requestsCount));

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d("REQUEST_COUNT_FAIL", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void getTotalOrdersCount() {
        Query query = FirebaseRef.getRequestsRef().child(serviceProvider.getUserId()).orderByChild("status").equalTo(2);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long ordersCount = dataSnapshot.getChildrenCount();

                    binding.countTotalOrders.setText(String.valueOf(ordersCount));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d("REQUEST_COUNT_FAIL", "onCancelled: " + error.getMessage());
            }
        });
    }


    private void openMessageRequests() {
        if (requestsCount != 0) {

            // Open bottom sheet
            MessageRequestsSheet sheet = new MessageRequestsSheet();
            sheet.show(mContext.getSupportFragmentManager(), MessageRequestsSheet.TAG);

        } else Toast.makeText(mContext, "No message request", Toast.LENGTH_SHORT).show();

    }


}
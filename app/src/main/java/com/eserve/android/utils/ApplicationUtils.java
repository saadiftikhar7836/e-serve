package com.eserve.android.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.eserve.android.R;
import com.eserve.android.model.MessageRequest;
import com.eserve.android.model.Navigation;
import com.eserve.android.model.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ApplicationUtils {

    /*******************
     * Constants
     */
    public static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    public static final String ERROR_TAG = "RESPONSE_FAILURE";
    public static final int IMAGE_PERMISSION_CODE = 1000;
    public static final int PICK_IMAGE_CODE = 1001;
    public static final int CIRCLE_RADIUS_3km = 3000;
    public static final int CIRCLE_RADIUS_40m = 40;
    public static final String DEFAULT_IMAGE = "https://firebasestorage.googleapis.com/v0/b/e-serve-ad552.appspot.com/o/default_profile_img.png?alt=media&token=d22f1132-2af1-4673-bbbb-96d5f4eb80d8";
    public static final String PRIVACY_POLICY_URL = "https://www.freeprivacypolicy.com/live/581fd5fa-72e0-4e6d-912b-2c4771f0afe6";

    private Context mContext;

    private static ApplicationUtils applicationUtils;


    //    Constructor
    public ApplicationUtils(Context ctx) {
        this.mContext = ctx;
    }


    public static ApplicationUtils getInstance(Context context) {
        if (applicationUtils == null) {
            applicationUtils = new ApplicationUtils(context.getApplicationContext());
        }
        return applicationUtils;

    }

    /***********************************************
     * Method for network is in working state or not.
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        }
        return false;
    }

    /*******************************
     * Hide keyboard from edit text
     */
    public static void hideKeyboard(Activity context) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = context.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /***********************************
     * Generate transparent random color
     */
    public int getTransRandomColor() {
        // create object of Random class
        Random obj = new Random();
        int randNum = obj.nextInt(0xffffff + 1);

        // format it as hexadecimal string and print
        String colorCode = String.format("%06x", randNum);
        return Color.parseColor("#1F" + colorCode);
    }

    /***********************
     * Set status bar white
     */
    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(color));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradient(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = ContextCompat.getDrawable(activity, R.drawable.bg_white);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    //    Save current user
    public static void saveUserToPreference(Context mContext, User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);

        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        prefManager.storeSharedData(mContext.getString(R.string.intent_user), json);
    }

    //    Get current user
    public static User getUserDetail(Context mContext) {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        String json = prefManager.getSharedData(mContext.getString(R.string.intent_user));

        Gson gson = new Gson();
        return gson.fromJson(json, User.class);
    }

    //    Save customer request
    public static void saveRequestToPreference(Context mContext, MessageRequest messageRequest) {
        Gson gson = new Gson();
        String json = gson.toJson(messageRequest);

        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        prefManager.storeSharedData(Constants.MESSAGE_REQUESTS, json);
    }

    //    Get customer request
    public static MessageRequest getUserRequest(Context mContext) {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        String json = prefManager.getSharedData(Constants.MESSAGE_REQUESTS);

        return new Gson().fromJson(json, MessageRequest.class);
    }
    public static void saveNavigationToPreference(Context mContext, Navigation navigation) {
        Gson gson = new Gson();
        String json = gson.toJson(navigation);

        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        prefManager.storeSharedData(Constants.CURRENT_NAVIGATION, json);
    }

    //    Get current user
    public static Navigation getNavigation(Context mContext) {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(mContext);
        String json = prefManager.getSharedData(Constants.CURRENT_NAVIGATION);

        return new Gson().fromJson(json, Navigation.class);
    }

    /*********************************************************************************************************************************************************
     *                                                                       Regex methods
     ********************************************************************************************************************************************************/

    public static boolean isValidName(String name) {
        Pattern pattern = Pattern.compile("^([a-zA-Z]{1,}\\s[a-zA-z]{1,}'?-?[a-zA-Z]{1,}\\s?([a-zA-Z]{1,})?)");
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

    public static boolean isValidAddress(String address) {
        Pattern pattern = Pattern.compile("\\d{1,5}\\s\\w.\\s(\\b\\w*\\b\\s){1,2}\\w*\\.");
        Matcher matcher = pattern.matcher(address);
        return matcher.find();
    }

    public static boolean isValidWebAddress(String url) {
        Pattern pattern = Pattern.compile("(([A-Za-z]{3,9}:(?://)?)(?:[-;:&=+$,\\\\w]+@)?[A-Za-z0-9.-]+|(?:www\\\\.|[-;:&=+$,\\\\w]+@)[A-Za-z0-9.-]+)((?:/[+~%/.\\\\w-]*)?\\\\??(?:[-+=&;%@.\\\\w]*)#?(?:[.!/\\\\\\\\\\\\w]*))?");
        Matcher matcher = pattern.matcher(url);
        return matcher.find();
    }

    public static boolean isValidEmail(String emailStr) {
        Pattern pattern = Pattern.compile("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        Matcher matcher = pattern.matcher(emailStr);
        return matcher.find();
    }


    public static boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}");
        return pattern.matcher(password).find();
    }

    public static String getAddress(Context context,Double latitude, Double longitude) {
        List<Address> addressList;
        String address = "";

        Geocoder geocoder = new Geocoder(context);
        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 5);
            if (addressList != null && addressList.size() > 0)
                address = addressList.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

}

package com.codesses.e_serve.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.UUID;

public class FirebaseRef {


    /****************************
     *   Firebase Authentication
     * @return
     */

    //   Firebase auth
    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }


    //   Current firebase user
    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }


    //   Current user id
    public static String getUserId() {
        return Objects.requireNonNull(getAuth().getCurrentUser()).getUid();
    }

    //   Current User Email
    public static String getUserEmail() {
        return Objects.requireNonNull(getAuth().getCurrentUser()).getEmail();
    }


    /*******************************
     *   Firebase Real Time Database
     * @return
     */

    //   Database instance
    public static DatabaseReference getDatabaseInstance() {
        return FirebaseDatabase.getInstance().getReference();
    }

    //    User reference
    public static DatabaseReference getUserRef() {
        return getDatabaseInstance().child("users");
    }


    //    Requests reference
    public static DatabaseReference getRequestsRef() {
        return getDatabaseInstance().child("requests");
    }


    public static DatabaseReference getNotificationRef() {
        return getDatabaseInstance().child("notifications");
    }

    public static DatabaseReference getMessageRef() {
        return getDatabaseInstance().child("messages");
    }

    public static DatabaseReference getNavigationRef() {
        return getDatabaseInstance().child("navigation");
    }

    public static DatabaseReference getRatingRef() {
        return getDatabaseInstance().child("ratings");
    }


    /****************************
     *   Firebase Storage
     * @return
     */

    //    Get storage instance
    public static StorageReference getStorageInstance() {
        return FirebaseStorage.getInstance().getReference();
    }


    //    Post storage reference
    public static StorageReference getPostStorage() {
        return getStorageInstance().child("Posts/").child("Images/" + UUID.randomUUID().toString());
    }

    //    user profile image reference
    public static StorageReference getProfileStorage() {
        return getStorageInstance().child("users/profiles");
    }

}

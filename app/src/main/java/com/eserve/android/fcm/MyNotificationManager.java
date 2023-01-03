package com.eserve.android.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.eserve.android.R;
import com.eserve.android.activity.ChatRoomActivity;
import com.eserve.android.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class MyNotificationManager extends ContextWrapper {

    public static final int NOTIFICATION_ID = 1000;

    static Intent intent;
    static Bitmap bitmapImage;
    NotificationManager notificationManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public MyNotificationManager(Context base) {
        super(base);
        createChannel();
    }


    public static void showNotification(Context mCtx, String title, String body, Map<String, String> data) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mCtx, "MyNotifications")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setAutoCancel(true)
                .setLights(Color.BLUE, 500, 500)
                .setVibrate(new long[]{500, 500, 500})
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(title)
                .setContentText(body)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Glide.with(mCtx).asBitmap().load(data.get("user_image"))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull @NotNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        builder.setLargeIcon(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {

                    }
                });


        if (title.equals("message")) {
            intent = new Intent(mCtx, ChatRoomActivity.class);
            intent.putExtra(Constants.USER_ID, data.get("c_id"));
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(mCtx, 1000, intent, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);



        //        FOR OREO AND GREATER VERSIONS

        MyNotificationManager.getManager(mCtx).notify(1000, builder.build());

    }

    public static NotificationManager getManager(Context mContext) {
        NotificationManager notificationManager = null;
        if (notificationManager == null) {
            notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel channel = new NotificationChannel("MyNotifications",
                "MyNotifications",
                NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager(getApplicationContext()).createNotificationChannel(channel);
    }

}

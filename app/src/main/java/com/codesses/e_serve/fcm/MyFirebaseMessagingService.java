package com.codesses.e_serve.fcm;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.codesses.e_serve.R;
import com.codesses.e_serve.activity.ChatRoomActivity;
import com.codesses.e_serve.activity.RoutingActivity;
import com.codesses.e_serve.utils.Constants;
import com.codesses.e_serve.utils.SharedPrefManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "Bestmarts";
    private static final String CHANNEL_NAME = "Bestmarts";
    Intent resultIntent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendNotification1(remoteMessage);
        } else {
            sendNotification(remoteMessage);
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        SharedPrefManager prefManager = SharedPrefManager.getInstance(getApplicationContext());
        prefManager.storeSharedData(getApplicationContext().getString(R.string.intent_fcm_token), s);
    }

    //    TODO: Function for sending notification to the users of android version lower than oreo
    @SuppressLint("LongLogTag")
    private void sendNotification(RemoteMessage remoteMessage) {
        if (isAppIsInBackground(getApplicationContext())) {
            //foreground app
            Log.e("remoteMessage foreground", remoteMessage.getData().toString());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            if (title.equals("message")) {
                resultIntent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                resultIntent.putExtra(Constants.USER_ID, remoteMessage.getData().get("c_id"));
            } else if (title.equals("Reached")) {
                resultIntent = new Intent(getApplicationContext(), RoutingActivity.class);
            }

            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    1 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setNumber(10)
                    .setTicker("Bestmarts")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(pendingIntent)
                    .setContentInfo("Info");

            notificationManager.notify(1, notificationBuilder.build());
        } else {
            Log.e("remoteMessage background", remoteMessage.getData().toString());
            Map data = remoteMessage.getData();
            String title = String.valueOf(data.get("title"));
            String body = String.valueOf(data.get("body"));
            if (title.equals("message")) {
                resultIntent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                resultIntent.putExtra(Constants.USER_ID, remoteMessage.getData().get("c_id"));
            } else if (title.equals("Reached")) {
                resultIntent = new Intent(getApplicationContext(), RoutingActivity.class);
            }

            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    1 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setNumber(10)
                    .setTicker("Bestmarts")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(pendingIntent)
                    .setContentInfo("Info");

            notificationManager.notify(1, notificationBuilder.build());
        }
    }

    //    TODO: Function for checking application is in background or not
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }

        return !isInBackground;
    }

    //    TODO: Function for sending notification to the users of android version equal or greater than oreo
    @SuppressLint("NewApi")
    private void sendNotification1(RemoteMessage remoteMessage) {
        if (isAppIsInBackground(getApplicationContext())) {
            //foreground app
            Log.e("remoteMessage", remoteMessage.getData().toString());
            String title = Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
            String body = remoteMessage.getNotification().getBody();
            if (title.equals("message")) {
                resultIntent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                resultIntent.putExtra(Constants.USER_ID, remoteMessage.getData().get("c_id"));
            } else if (title.equals("Reached")) {
                resultIntent = new Intent(getApplicationContext(), RoutingActivity.class);
            }

            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    1 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Uri defaultsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            OreoNotification oreoNotification = new OreoNotification(this);
            Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultsound, String.valueOf(R.mipmap.ic_launcher_foreground));

            int i = 1;
            oreoNotification.getManager().notify(i, builder.build());


        } else {
            Log.e("remoteMessage", remoteMessage.getData().toString());
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            if (title.equals("message")) {
                resultIntent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                resultIntent.putExtra(Constants.USER_ID, remoteMessage.getData().get("c_id"));
            } else if (title.equals("Reached")) {
                resultIntent = new Intent(getApplicationContext(), RoutingActivity.class);
            }

            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    1 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Uri defaultsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            OreoNotification oreoNotification = new OreoNotification(this);
            Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultsound, String.valueOf(R.mipmap.ic_launcher_foreground));
            int i = 1;
            oreoNotification.getManager().notify(i, builder.build());
        }

    }


}

package io.github.yuvrajsab.babblin;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("get_user_id");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int notificationId = (int) System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel("friend_channel", "Friend", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.CYAN);
            channel.setDescription("Friend Request notification");
            channel.canShowBadge();
            channel.setShowBadge(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id", from_user_id);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "friend_channel");
        builder.setContentTitle(notification_title);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText(notification_message);
        builder.setContentIntent(pendingIntent);



        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }

    }
}

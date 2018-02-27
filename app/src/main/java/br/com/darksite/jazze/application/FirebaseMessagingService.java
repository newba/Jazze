package br.com.darksite.jazze.application;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import br.com.darksite.jazze.R;

/* Prende notifications
https://developer.android.com/guide/topics/ui/notifiers/notifications.html
https://www.androidauthority.com/how-to-create-android-notifications-707254/
https://developer.android.com/training/notify-user/build-notification.html
https://firebase.google.com/docs/cloud-messaging/android/client
 */


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_body = remoteMessage.getNotification().getBody();

        String click_action = remoteMessage.getNotification().getClickAction();
        String from_sender_id = remoteMessage.getData().get("from_sender_id").toString();


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)

                        .setSmallIcon(R.mipmap.jazze)
                        .setContentTitle(notification_title)
                        .setContentText(notification_body);

        //Clique sur notification
        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("destinataireUtilisateurUid", from_sender_id);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


        // Sets an ID for the notification
        int mNotificationId = (int) System.currentTimeMillis(); //random unique keys
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
}

package com.oxiane.tp_gcm;

import java.text.SimpleDateFormat;
import java.util.List;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
    public static final String NOTIF_RECEIVED = "ACTIVITY_NOTIF_RECEIVED";
	public static final int     NOTIFICATION_ID = 1;
    private static final String TAG             = "test";
	public static final String MESSAGE_RECU_IN_APP_RECEIVER = "in_app";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder  builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        
        {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) { // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that
             * GCM will be extended in the future with new message types, just
             * ignore any message types you're not interested in, or that you
             * don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
//                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
//                sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                Log.i(TAG, "registration_ids:["+extras.getString("registration_ids")+"]" );
                Log.i(TAG, "data:["+extras.getString("Notice")+"]");
                
                if(extras.getString("Notice") != null)
                {
	                sendNotification(extras.getString("Notice"), extras.getString("From"));
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void sendNotification(String message, String from) {
    	
    	mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        long[] pattern = { 100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200 };
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Nouveau message de "+from)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setVibrate(pattern)
                .setLights(0xFFFF00FF, 100, 3000)
                .setSound(uri)
                .setContentText(message);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        
        updateMyActivityWithMessage(getApplicationContext(), from, message);
	}
    
   

	private void updateMyActivityWithMessage(Context context, String from, String message) {
		Intent intent = new Intent(MESSAGE_RECU_IN_APP_RECEIVER);
		intent.putExtra("from", from);
		intent.putExtra("message", message);

        context.sendBroadcast(intent);
	}
	
}

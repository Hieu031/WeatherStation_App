/*** --- Function of module --- 
 * Service runs in the background, monitoring temperature from Firebase.
 * If the temperature exceeds a threshold, send an alert notification.
 */

 package com.example.myapplication.Activitis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.Service;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

// Create a class to implement service run in background
public class FirebaseMonitoringService extends Service {
    private static final String CHANNEL_ID = "TemperatureMonitoringChannel";
    private static final int NOTIFICATION_ID = 123;

    private DatabaseReference temperatureRef;

    @Override
    public void onCreate() {
        super.onCreate();

        // Connect to Firebase Realtime Database và Take path to temperature
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        temperatureRef = database.getReference("Temperature");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Listen to temperature data changes on Firebase
        temperatureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get data from dataSnapshot và assign to temperature
                Double temperature = dataSnapshot.getValue(Double.class);
                if (temperature != null) {
                    checkTemperature(temperature);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(FirebaseMonitoringService.this, "Không thể đọc dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
            }
        });

        return START_STICKY;
    }

    // Function check temperature exceeds the threshold?
    private void checkTemperature(double temperature) {
        if (temperature > 40) {
            sendNotification("Nhiệt độ cao", "Nhiệt độ hiện tại là " + temperature + " độ C. Vui lòng kiểm tra.");
        }
    }

    private void sendNotification(String title, String message) {
        // Create a NotificationManager to notify
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //  Create a NotificationChannel (Just do it on Android Oreo or later)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Temperature Monitoring Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notify to smartphone of user
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.cloudy_sunny)
                .build();

        // Show notification
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


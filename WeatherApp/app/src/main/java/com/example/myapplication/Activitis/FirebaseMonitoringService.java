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

import java.util.Locale;

// Create a class to implement service run in background
public class FirebaseMonitoringService extends Service {
    private static final String CHANNEL_ID = "TemperatureMonitoringChannel";
    private static final int NOTIFICATION_ID = 123;
    private static final String DB_URL =
            "https://autosar01-default-rtdb.asia-southeast1.firebasedatabase.app";
    private DatabaseReference temperatureRef;
    private static final double THRESHOLD = 40.0; /* Threshold temp logic */
    private static final long COOL_DOWN_MS = 10 * 60 * 1000;
    /* Anti-spam: report back after at least 10 minutes */
    private long lastNotifyTime = 0L;
    private ValueEventListener temperatureListener;
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        // Connect to Firebase Realtime Database và Take path to temperature
        FirebaseDatabase database = FirebaseDatabase.getInstance(DB_URL);
        temperatureRef = database.getReference("WeatherCurrent").child("Temperature");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Listen to temperature data changes on Firebase
//        temperatureRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot ds) {
//                // Get data from dataSnapshot và assign to temperature
////                Double temperature = dataSnapshot.getValue(Double.class);
//                Double temperature = toDouble(ds.getValue());
//                if (temperature != null) {
//                    checkTemperature(temperature);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(FirebaseMonitoringService.this, "Không thể đọc dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
//            }
//        });
        temperatureListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                Double temperature = toDouble(ds.getValue());
                if (temperature != null) {
                    checkTemperature(temperature);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Bạn có thể log hoặc gửi 1 notification nhẹ nếu cần
            }
        };

        temperatureRef.addValueEventListener(temperatureListener);
        return START_STICKY;
    }

    // Function check temperature exceeds the threshold?
    private void checkTemperature(double temperature) {
//        if (temperature > 40) {
//            sendNotification("Nhiệt độ cao", "Nhiệt độ hiện tại là " + temperature + " độ C. Vui lòng kiểm tra.");
//        }
        if (temperature > THRESHOLD) {
            long now = System.currentTimeMillis();
            if (now - lastNotifyTime >= COOL_DOWN_MS) {
                lastNotifyTime = now;
                String title = "Nhiệt độ cao";
                String msg = String.format(Locale.US,
                        "Nhiệt độ hiện tại là %.1f°C. Vui lòng kiểm tra.", temperature);
                sendNotification(title, msg);
            }
        }
    }

    private void sendNotification(String title, String message) {
        // Create a NotificationManager to notify
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
//        //  Create a NotificationChannel (Just do it on Android Oreo or later)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "Temperature Monitoring Channel",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            nm.createNotificationChannel(channel);
//        }
//
//        // Create a notify to smartphone of user
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setSmallIcon(R.drawable.cloudy_sunny)
//                .build();
//
//        // Show notification
//        nm.notify(NOTIFICATION_ID, notification);
        // Khi người dùng bấm thông báo → mở MainActivity1 (sửa Activity tuỳ bạn)
        Intent openIntent = new Intent(this, MainActivity1.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, openIntent,
                Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.cloudy_sunny)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        nm.notify(NOTIFICATION_ID, notification);
    }

    private Notification buildStatusNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.cloudy_sunny)
                .setContentTitle("Giám sát nhiệt độ")
                .setContentText(text)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Temperature Monitoring",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Cảnh báo khi nhiệt độ vượt ngưỡng");
            nm.createNotificationChannel(channel);
        }
    }

    // Chuyển mọi kiểu Firebase (Long/Double/String) → Double an toàn
    private Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); }
        catch (Exception e) { return null; }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (temperatureRef != null & temperatureListener != null){
            temperatureRef.removeEventListener(temperatureListener);
        }
    }
}


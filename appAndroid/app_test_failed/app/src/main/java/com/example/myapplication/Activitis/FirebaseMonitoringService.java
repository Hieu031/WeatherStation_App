///*** --- Function of module ---
// * Service runs in the background, monitoring temperature from Firebase.
// * If the temperature exceeds a threshold, send an alert notification.
// */
//
//package com.example.myapplication.Activitis;
//
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.IBinder;
//
//import androidx.annotation.NonNull;
//import androidx.core.app.NotificationCompat;
//
//import com.example.myapplication.R;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.messaging.FirebaseMessagingService;
//import com.google.firebase.messaging.RemoteMessage;
//
//import android.app.Service;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.core.app.NotificationManagerCompat;
//
//import java.util.Locale;
//
//// Create a class to implement service run in background
//public class FirebaseMonitoringService extends Service {
//    private static final String CHANNEL_ID = "TemperatureMonitoringChannel";
//    private static final int NOTI_FOREGROUND_ID = 123;
//    private static final int NOTI_ALERT_ID = 124;     // ID riêng cho cảnh báo
//    private static final String DB_URL =
//            "https://autosar01-default-rtdb.asia-southeast1.firebasedatabase.app";
//    private static final double THRESHOLD = 40.0; /* Threshold temp logic */
//    private static final long COOL_DOWN_MS = 10 * 60 * 1000;
//    /* Anti-spam: report back after at least 10 minutes */
//    private long lastNotifyTime = 0L;
//    private DatabaseReference temperatureRef;
//    private ValueEventListener temperatureListener;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        createNotificationChannel();
//        // Connect to Firebase Realtime Database và Take path to temperature
//        FirebaseDatabase db = FirebaseDatabase.getInstance(DB_URL);
//        temperatureRef = db.getReference("Temperature");
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // Listen to temperature data changes on Firebase
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
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(FirebaseMonitoringService.this, "Không thể đọc dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
//            }
//        });
//        return START_STICKY;
//    }
//
//    // Function check temperature exceeds the threshold?
//    private void checkTemperature(double t) {
//        if (t > THRESHOLD) {
//            long now = System.currentTimeMillis();
//            if (now - lastNotifyTime >= COOL_DOWN_MS) {
//                lastNotifyTime = now;
//                sendAlert("Nhiệt độ cao",
//                        String.format(Locale.US, "Nhiệt độ hiện tại là %.1f°C. Vui lòng kiểm tra.", t));
//            }
//        }
//    }
//
//    private void sendAlert(String title, String msg) {
//        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent open = new Intent(this, MainActivity1.class)
//                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pi = PendingIntent.getActivity(this, 0, open,
//                Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
//
//        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.cloudy_sunny)
//                .setContentTitle(title)
//                .setContentText(msg)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setDefaults(NotificationCompat.DEFAULT_ALL)
//                .setAutoCancel(true)
//                .setContentIntent(pi)
//                .build();
//
//        nm.notify(NOTI_ALERT_ID, n); // dùng ID khác foreground
//    }
//    private Notification buildStatusNotification(String text) {
//        return new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.cloudy_sunny)
//                .setContentTitle("Giám sát nhiệt độ")
//                .setContentText(text)
//                .setOngoing(true)
//                .setPriority(NotificationCompat.PRIORITY_MIN)
//                .build();
//    }
//
//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel ch = new NotificationChannel(
//                    CHANNEL_ID, "Temperature Monitoring", NotificationManager.IMPORTANCE_DEFAULT);
//            ch.setDescription("Cảnh báo khi nhiệt độ vượt ngưỡng");
//            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
//                    .createNotificationChannel(ch);
//        }
//    }
//
//    private @Nullable Double toDouble(Object v) {
//        if (v == null) return null;
//        if (v instanceof Number)
//            return ((Number) v).doubleValue();
//        try {
//            return Double.parseDouble(v.toString().trim());
//        }
//        catch (Exception e)
//        {
//            return null;
//        }
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (temperatureRef != null && temperatureListener != null){
//            temperatureRef.removeEventListener(temperatureListener);
//        }
//        stopForeground(true);
//    }
//}
//
/*** --- Function of module ---
 * Service runs in the background, monitoring Temperature from Firebase.
 * If temperature exceeds a threshold, send an alert notification.
 */
package com.example.myapplication.Activitis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class FirebaseMonitoringService extends Service {

    // === Config ===
    private static final String DB_URL =
            "https://autosar01-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String CHANNEL_ID = "TemperatureMonitoringChannel";
    private static final int NOTI_ID_FOREGROUND = 123; // ongoing (foreground)
    private static final int NOTI_ID_ALERT      = 124; // alert when > threshold

    private static final double THRESHOLD   = 40.0;          // °C
    private static final double HYSTERESIS  = 1.0;           // avoid chattering
    private static final long   COOL_DOWN_MS = 10 * 60 * 1000; // 10 minutes

    // === Runtime ===
    private DatabaseReference temperatureRef;
    private ValueEventListener tempListener;
    private boolean isHot = false;
    private long lastNotifyTime = 0L;

    @Override
    public void onCreate() {
        super.onCreate();

        // 1) Channel for both foreground + alerts
        createNotificationChannel();

        // 2) Firebase path (root -> "Temperature")
        FirebaseDatabase db = FirebaseDatabase.getInstance(DB_URL);
        temperatureRef = db.getReference("Temperature");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 3) Must enter foreground immediately on Android O+
        startForeground(NOTI_ID_FOREGROUND, buildStatusNotification("Đang giám sát…"));

        // 4) Attach listener (idempotent)
        if (tempListener == null) {
            tempListener = new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot ds) {
                    Double t = toDouble(ds.getValue());
                    android.util.Log.d("TempService", "Temperature raw=" + ds.getValue() + " -> " + t);
                    if (t != null) checkTemperature(t);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {
                    android.util.Log.e("TempService", "RTDB error: " + e.getMessage());
                }
            };
            temperatureRef.addValueEventListener(tempListener);
        }

        return START_STICKY;
    }

    private void checkTemperature(double t) {
        if (!isHot && t > THRESHOLD) {
            isHot = true;
            long now = System.currentTimeMillis();
            if (now - lastNotifyTime >= COOL_DOWN_MS) {
                lastNotifyTime = now;
                sendAlert(
                        "Nhiệt độ cao",
                        String.format(Locale.US, "Nhiệt độ hiện tại là %.1f°C. Vui lòng kiểm tra.", t)
                );
            }
        } else if (isHot && t < THRESHOLD - HYSTERESIS) {
            isHot = false;
        }
    }

    private Notification buildStatusNotification(String text) {
        Intent open = new Intent(this, MainActivity1.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, open,
                Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.cloudy_sunny)
                .setContentTitle("Giám sát nhiệt độ")
                .setContentText(text)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(pi)
                .build();
    }

    private void sendAlert(String title, String message) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        Intent open = new Intent(this, MainActivity1.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, open,
                Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);

        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.cloudy_sunny)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // sound/vibrate/lights
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build();

        nm.notify(NOTI_ID_ALERT, n);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Temperature Monitoring",
                    NotificationManager.IMPORTANCE_DEFAULT // foreground uses this; alerts bump priority in builder
            );
            ch.setDescription("Cảnh báo khi nhiệt độ vượt ngưỡng");
            nm.createNotificationChannel(ch);
        }
    }

    // Accept String/Long/Double safely
    @Nullable
    private static Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(v.toString().trim()); }
        catch (Exception e) { return null; }
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (temperatureRef != null && tempListener != null) {
            temperatureRef.removeEventListener(tempListener);
        }
        stopForeground(true);
    }
}

/*** --- Function of module ---
 * Service runs in the background, monitoring Temperature from Firebase.
 * If temperature exceeds a threshold, send an alert notification.
 */
package com.example.myapplication.Activitis;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
    private int alertCounter = 1000; // stack alert
    private static final int NOTI_ID_FOREGROUND = 123; // ongoing (foreground)
    private static final int NOTI_ID_ALERT      = 124; // alert when > threshold

    private static final double THRESHOLD   = 40.0;          // °C
    private static final double HYSTERESIS  = 0.5;           // avoid chattering
    private static final long   COOL_DOWN_MS =  10 * 1000; // 10 seconds

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
        startForeground(NOTI_ID_FOREGROUND, buildStatusNotification("Đang giám sát nhiệt độ…"));

        // 4) Attach listener (idempotent)
        if (tempListener == null) {
            tempListener = new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot ds) {
                    Double t = toDouble(ds.getValue());
                    android.util.Log.d("TempService", "Temperature raw=" + ds.getValue() + " -> " + t);
                    if (t == null) return;
                    updateStatusNotification(t);
                    checkTemperature(t);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {
                    Toast.makeText(FirebaseMonitoringService.this, "Lỗi giám sát nhiệt độ!", Toast.LENGTH_SHORT).show();
                }
            };
            temperatureRef.addValueEventListener(tempListener);
        }

        return START_STICKY;
    }

    private void checkTemperature(double t) {
        long now = System.currentTimeMillis();

        if (t >= THRESHOLD) {
            if (!isHot) {
                // Rising edge: vừa vượt ngưỡng
                isHot = true;
                lastNotifyTime = now;
                sendAlertStack("Nhiệt độ cao",
                        String.format(Locale.getDefault(), "Nhiệt độ hiện tại là %.1f°C. Vui lòng kiểm tra.", t));
            } else if (now - lastNotifyTime >= COOL_DOWN_MS) {
                // Đang ở vùng nóng và đã qua cooldown -> nhắc lại định kỳ
                lastNotifyTime = now;
                sendAlertStack("Nhiệt độ vẫn cao",
                        String.format(Locale.getDefault(), "Hiện tại: %.1f°C", t));
            }
        } else if (t <= THRESHOLD - HYSTERESIS) {
            // Hạ đủ thấp -> reset để lần sau vượt lên sẽ bắn lại
            isHot = false;
        }
    }

    /* Create notification "status" */
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

    /** update notification foreground with new value */
    private void updateStatusNotification(double t) {
        String text = String.format(Locale.getDefault(), "Hiện tại: %.1f°C", t);
        Notification n = buildStatusNotification(text);

        // Cách 1 (ổn định trên đa số máy): notify lại cùng ID
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        NotificationManagerCompat.from(this).notify(NOTI_ID_FOREGROUND, n);
    }

    private void sendAlertStack(String title, String message) {
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
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build();

        // ID mới cho mỗi cảnh báo -> tạo thông báo mới thay vì cập nhật cái cũ
        nm.notify(alertCounter++, n);
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

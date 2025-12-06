package com.example.weather.Activitis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.weather.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class FirebaseMonitoringService extends Service {
    private static final String CHANNEL_ID = "TemperatureMonitoringChannel";
    private static final int NOTIFICATION_ID = 123;

    private DatabaseReference temperatureRef;

    @Override
    public void onCreate() {
        super.onCreate();

        // Kết nối với Firebase Realtime Database và lấy đường dẫn đến nút nhiệt độ
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        temperatureRef = database.getReference("Temperature");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Lắng nghe sự thay đổi của dữ liệu nhiệt độ trên Firebase
        temperatureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Lấy giá trị nhiệt độ từ dataSnapshot và gán cho biến temperature
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

    private void checkTemperature(double temperature) {
        if (temperature > 40) {
            sendNotification("Nhiệt độ cao", "Nhiệt độ hiện tại là " + temperature + " độ C. Vui lòng kiểm tra.");
        }
    }

    private void sendNotification(String title, String message) {
        // Tạo một NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo một NotificationChannel (chỉ cần thực hiện trên Android Oreo trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Temperature Monitoring Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo một thông báo
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.cloudy_sunny)
                .build();

        // Hiển thị thông báo
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


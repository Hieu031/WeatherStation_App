package com.example.weather.Activitis;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weather.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewInterfaceActivity extends AppCompatActivity {

    TextView tv1, tv2;
    Button back;
    DatabaseReference mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_interface);

        tv1 = findViewById(R.id.textView);
        tv2 = findViewById(R.id.textView2);
        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại MainActivity khi nhấn nút back
                onBackPressed();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("doam");
        DatabaseReference myRefNhietDo = database.getReference("nhietdo");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String message = dataSnapshot.getValue(String.class);
                // Cập nhật giao diện trên luồng giao diện chính
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Hiển thị dữ liệu lên TextView
                        tv1.setText(message);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
        myRefNhietDo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nhietDo = dataSnapshot.getValue(String.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv2.setText("Nhiệt độ: " + nhietDo); // Hiển thị nhiệt độ trên tv2
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Quay lại MainActivity khi nhấn nút back
        Intent intent = new Intent(NewInterfaceActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Đóng Activity hiện tại
    }
}
/*** --- Function of module --- 
 * Display data from Firebase for a specific location
 * Can turn back "MainActivity"
 */

package com.example.myapplication.Activitis;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class NewInterfaceActivity extends AppCompatActivity {

    TextView tv1, tv2;
    Button back;

    private static final String DB_URL =
        "https://autosar01-default-rtdb.asia-southeast1.firebasedatabase.app";

    private DatabaseReference myRefHumid, myRefTemp;
    private ValueEventListener humidL, tempL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_interface);

        tv1 = findViewById(R.id.textView);
        tv2 = findViewById(R.id.textView2);
        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return Screen MainActivity
                startActivity(new Intent(NewInterfaceActivity.this, MainActivity.class));
                finish();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance(DB_URL);
        DatabaseReference root = database.getReference("WeatherCurrent");
        myRefHumid = root.child("Humidity");
        myRefTemp = root.child("Temperature");

//        myRefHumid.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot ds) {
////                String message = dataSnapshot.getValue(String.class);
//                String Humid = String.valueOf(ds.getValue());
//                // Update interface at main flow
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        // Display data in tv1
////                        tv1.setText("Độ ẩm: " + Humid + "%");
////                    }
////                });
//                runOnUiThread(() -> tv1.setText("Độ ẩm: " + Humid + "%"));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Handler error (if have)
//            }
//        });
//        myRefTemp.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot ds) {
////                String nhietDo = dataSnapshot.getValue(String.class);
//                String nhietDo = String.valueOf(ds.getValue());
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        tv2.setText("Nhiệt độ: " + nhietDo); // Display temperature in tv2
////                    }
////                });
//                runOnUiThread(() -> tv2.setText("Nhiệt độ: " + nhietDo + "°C"));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Handler error (if have)
//            }
//        });
        humidL = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot ds) {
                Object v = ds.getValue();
                String s = (v == null) ? "--" : String.valueOf(v);
                try {
                    // format số nếu có thể (không bắt buộc)
                    double d = Double.parseDouble(s);
                    s = String.format(Locale.US, "%.0f", d);
                } catch (Exception ignored) {}
                tv1.setText("Độ ẩm: " + s + "%");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { /* TODO: log/Toast nếu cần */ }
        };

        tempL = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot ds) {
                Object v = ds.getValue();
                String s = (v == null) ? "--" : String.valueOf(v);
                try {
                    double d = Double.parseDouble(s);
                    s = String.format(Locale.US, "%.1f", d);
                } catch (Exception ignored) {}
                tv2.setText("Nhiệt độ: " + s + "°C");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { /* TODO */ }
        };

        myRefHumid.addValueEventListener(humidL);
        myRefTemp.addValueEventListener(tempL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dọn listener khi rời màn
        if (myRefHumid != null && humidL != null) myRefHumid.removeEventListener(humidL);
        if (myRefTemp  != null && tempL  != null) myRefTemp.removeEventListener(tempL);
    }
}

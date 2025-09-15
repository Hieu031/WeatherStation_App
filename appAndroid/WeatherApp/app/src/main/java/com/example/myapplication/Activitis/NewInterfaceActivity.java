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

public class NewInterfaceActivity extends AppCompatActivity {

    TextView tv1, tv2;
    Button back;
//    DatabaseReference mData;
    /* Keep reference and  Listener to exit when destroy */
    private FirebaseDatabase db;
    private DatabaseReference refHumid, refTemp;
    private ValueEventListener humidListener, tempListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_interface);

        tv1 = findViewById(R.id.textView);
        tv2 = findViewById(R.id.textView2);
        back = findViewById(R.id.back);

        back.setOnClickListener(v -> onBackPressed());
        /* Init instance to Firebase */
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://autosar01-default-rtdb.asia-southeast1.firebasedatabase.app"
        );
        refHumid = db.getReference("Humidity");
        refTemp  = db.getReference("Temperature");

        /* Read type value safety */
        humidListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                String humid = toStr(s.getValue());
                tv1.setText(humid.isEmpty() ? "__" : humid); // Show humidity
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        tempListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                String temp = toStr(s.getValue());
                tv2.setText("Nhiệt độ: " + (temp.isEmpty() ? "__" : temp));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        refHumid.addValueEventListener(humidListener);
        refTemp.addValueEventListener(tempListener);
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String message = dataSnapshot.getValue(String.class);
//                // Update interface at main flow
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // Display data in tv1
//                        tv1.setText(message);
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Handler error (if have)
//            }
//        });
//        myRefNhietDo.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String nhietDo = dataSnapshot.getValue(String.class);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        tv2.setText("Nhiệt độ: " + nhietDo); // Display temperature in tv2
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Handler error (if have)
//            }
//        });
    }

    /* Remove listener to avoid leak */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refHumid != null && humidListener != null){
            refHumid.removeEventListener(humidListener);
        }
        if (refTemp != null && tempListener != null){
            refTemp.removeEventListener(tempListener);
        }
    }

    // Function return MainActivity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Intent intent = new Intent(NewInterfaceActivity.this, MainActivity.class);
//        startActivity(intent);
        finish(); 
    }

    /* Convert all type data into String type */
    private String toStr(Object v){
        return v == null ? "" : v.toString().trim();
    }
}